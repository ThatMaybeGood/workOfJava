package com.etl.service.reader;

import com.etl.entity.DatasourceConfig;
import com.etl.entity.EtlTaskConfig;
import com.etl.enums.HttpAuthType;
import com.etl.service.core.DataSourceManager;
import com.etl.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.util.Timeout;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Component
public class HttpReader implements DataSourceReader {

    private EtlTaskConfig taskConfig;
    private DataSourceManager dataSourceManager;
    private CloseableHttpClient httpClient;
    private int currentPage = 0;
    private boolean hasMore = true;

    @Override
    public String getSourceType() {
        return "HTTP";
    }

    @Override
    public void init(EtlTaskConfig task, DataSourceManager dataSourceManager) {
        this.taskConfig = task;
        this.dataSourceManager = dataSourceManager;
        this.httpClient = HttpClients.custom()
                .setDefaultRequestConfig(
                        RequestConfig.custom()
                                .setConnectTimeout(Timeout.ofMilliseconds(taskConfig.getHttpTimeout() != null ? taskConfig.getHttpTimeout() : 30000))
                                .setResponseTimeout(Timeout.ofMilliseconds(taskConfig.getHttpTimeout() != null ? taskConfig.getHttpTimeout() : 30000))
                                .build()
                )
                .build();
        this.currentPage = 0;
        this.hasMore = true;
    }

    @Override
    public List<Map<String, Object>> readAll() {
        List<Map<String, Object>> allResults = new ArrayList<>();

        if ("Y".equals(taskConfig.getHttpPagination())) {
            while (hasMore) {
                List<Map<String, Object>> batch = fetchPage();
                if (batch.isEmpty()) {
                    break;
                }
                allResults.addAll(batch);
            }
        } else {
            allResults.addAll(fetchPage());
        }

        return allResults;
    }

    @Override
    public List<Map<String, Object>> readBatch(int batchSize) {
        if (!hasMore) {
            return Collections.emptyList();
        }
        return fetchPage();
    }

    private List<Map<String, Object>> fetchPage() {
        try {
            String url = buildUrl();
            String method = taskConfig.getHttpMethod() != null ? taskConfig.getHttpMethod().toUpperCase() : "GET";

            HttpUriRequestBase request;
            if ("POST".equals(method)) {
                request = new HttpPost(url);
                if (taskConfig.getHttpBody() != null) {
                    request.setEntity(new StringEntity(taskConfig.getHttpBody(), StandardCharsets.UTF_8));
                }
            } else {
                request = new HttpGet(url);
            }

            // 设置请求头
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");

            if (taskConfig.getHttpHeaders() != null) {
                Map<String, String> headers = JsonUtil.fromJson(taskConfig.getHttpHeaders(), HashMap.class);
                headers.forEach(request::setHeader);
            }

            // 认证
            if (taskConfig.getHttpAuthType() != null) {
                applyAuth(request);
            }

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();
                String body = EntityUtils.toString(entity, StandardCharsets.UTF_8);

                return parseResponse(body);
            }
        } catch (Exception e) {
            log.error("HTTP请求失败", e);
            throw new RuntimeException("HTTP请求失败: " + e.getMessage(), e);
        }
    }

    private String buildUrl() {
        String url = taskConfig.getHttpUrl();
        if ("Y".equals(taskConfig.getHttpPagination())) {
            String pageParam = taskConfig.getHttpPageParam() != null ? taskConfig.getHttpPageParam() : "page";
            String sizeParam = taskConfig.getHttpSizeParam() != null ? taskConfig.getHttpSizeParam() : "size";
            int pageSize = taskConfig.getHttpPageSize() != null ? taskConfig.getHttpPageSize() : 1000;

            String separator = url.contains("?") ? "&" : "?";
            url = url + separator + pageParam + "=" + currentPage + "&" + sizeParam + "=" + pageSize;
            currentPage++;
        }
        return url;
    }

    private void applyAuth(HttpUriRequestBase request) {
        String authType = taskConfig.getHttpAuthType();
        if (HttpAuthType.BASIC.getValue().equals(authType)) {
            String auth = taskConfig.getHttpUsername() + ":" + taskConfig.getHttpPassword();
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            request.setHeader("Authorization", "Basic " + encodedAuth);
        } else if (HttpAuthType.TOKEN.getValue().equals(authType)) {
            request.setHeader("Authorization", "Bearer " + taskConfig.getHttpToken());
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseResponse(String body) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(body);

            String dataPath = taskConfig.getHttpDataPath();
            if (dataPath != null && !dataPath.isEmpty()) {
                // 支持简单的JSON路径，如 $.data.list
                String[] parts = dataPath.replace("$.", "").split("\\.");
                for (String part : parts) {
                    if (root != null && root.isObject()) {
                        root = root.get(part);
                    }
                }
            }

            if (root != null && root.isArray()) {
                List<Map<String, Object>> results = new ArrayList<>();
                for (JsonNode node : root) {
                    results.add(JsonUtil.mapToObject(JsonUtil.objectToMap(node), HashMap.class));
                }
                return results;
            }

            return Collections.emptyList();
        } catch (Exception e) {
            log.error("解析HTTP响应失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public long getTotalCount() {
        return -1;
    }

    @Override
    public boolean testConnection(DatasourceConfig config, DataSourceManager dataSourceManager) {
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpGet request = new HttpGet(config.getJdbcUrl());
            try (CloseableHttpResponse response = client.execute(request)) {
                return response.getCode() < 400;
            }
        } catch (Exception e) {
            log.warn("HTTP连接测试失败", e);
            return false;
        }
    }

    @Override
    public List<Map<String, Object>> preview(int limit) {
        return readAll().subList(0, Math.min(limit, readAll().size()));
    }

    @Override
    public void close() {
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (Exception e) {
                log.warn("关闭HTTP客户端失败", e);
            }
        }
    }
}
