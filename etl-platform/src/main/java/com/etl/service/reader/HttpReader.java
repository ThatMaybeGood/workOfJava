package com.etl.service.reader;

import com.etl.entity.DatasourceConfig;
import com.etl.entity.EtlTaskConfig;
import com.etl.enums.HttpAuthType;
import com.etl.service.core.DataSourceManager;
import com.etl.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
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
        String responseType = taskConfig.getHttpResponseType();
        if (responseType == null) responseType = "JSON";

        try {
            if ("XML".equalsIgnoreCase(responseType)) {
                return parseXmlResponse(body);
            } else {
                return parseJsonResponse(body);
            }
        } catch (Exception e) {
            log.error("解析HTTP响应失败, type={}", responseType, e);
            return Collections.emptyList();
        }
    }

    /**
     * 解析 JSON 格式响应
     */
    private List<Map<String, Object>> parseJsonResponse(String body) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(body);

        String dataPath = taskConfig.getHttpDataPath();
        if (dataPath != null && !dataPath.isEmpty()) {
            root = resolveJsonPath(root, dataPath);
        }

        if (root != null && root.isArray()) {
            List<Map<String, Object>> results = new ArrayList<>();
            for (JsonNode node : root) {
                results.add(JsonUtil.mapToObject(JsonUtil.objectToMap(node), HashMap.class));
            }
            return results;
        }

        if (root != null && root.isObject()) {
            List<Map<String, Object>> results = new ArrayList<>();
            results.add(JsonUtil.mapToObject(JsonUtil.objectToMap(root), HashMap.class));
            return results;
        }

        return Collections.emptyList();
    }

    /**
     * 解析 XML 格式响应 —— 使用 Jackson XML 将 XML 转为 JsonNode 再提取数据
     */
    private List<Map<String, Object>> parseXmlResponse(String body) throws Exception {
        XmlMapper xmlMapper = new XmlMapper();
        JsonNode root = xmlMapper.readTree(body.getBytes(StandardCharsets.UTF_8));

        // 使用 dataPath 定位数据节点
        String dataPath = taskConfig.getHttpDataPath();
        if (dataPath != null && !dataPath.isEmpty()) {
            root = resolveJsonPath(root, dataPath);
        }

        // XML 解析后可能是单对象或数组
        if (root != null && root.isArray()) {
            List<Map<String, Object>> results = new ArrayList<>();
            for (JsonNode node : root) {
                results.add(jsonNodeToFlatMap(node));
            }
            return results;
        }

        if (root != null && root.isObject()) {
            List<Map<String, Object>> results = new ArrayList<>();
            results.add(jsonNodeToFlatMap(root));
            return results;
        }

        return Collections.emptyList();
    }

    /**
     * 将 JsonNode 扁平化为 Map，处理 XML 解析的嵌套 "" 包装
     */
    private Map<String, Object> jsonNodeToFlatMap(JsonNode node) {
        Map<String, Object> row = new HashMap<>();
        if (node == null) return row;
        java.util.Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            JsonNode value = entry.getValue();
            if (value.isTextual()) {
                row.put(entry.getKey(), value.asText());
            } else if (value.isNumber()) {
                row.put(entry.getKey(), value.numberValue());
            } else if (value.isBoolean()) {
                row.put(entry.getKey(), value.asBoolean());
            } else if (value.isNull()) {
                row.put(entry.getKey(), null);
            } else if (value.isObject()) {
                // XML 解析可能产生嵌套对象，递归 1 层
                row.put(entry.getKey(), jsonNodeToFlatMap(value));
            } else if (value.isArray()) {
                row.put(entry.getKey(), value.toString());
            }
        }
        return row;
    }

    /**
     * 解析 JSONPath 定位数据数组，支持 $.a.b[0].c、a.b、$.list[0] 等形式。
     */
    private JsonNode resolveJsonPath(JsonNode root, String path) {
        String p = path.trim();
        if (p.startsWith("$")) {
            p = p.substring(1);
        }
        if (p.startsWith(".")) {
            p = p.substring(1);
        }
        if (p.isEmpty()) {
            return root;
        }

        JsonNode current = root;
        String[] segments = p.split("\\.");
        for (String seg : segments) {
            if (seg.isEmpty() || current == null) {
                continue;
            }
            // 处理数组下标: list[0]
            int bracket = seg.indexOf('[');
            if (bracket >= 0) {
                String field = seg.substring(0, bracket);
                String idxStr = seg.substring(bracket + 1, seg.indexOf(']'));
                if (!field.isEmpty()) {
                    current = current.get(field);
                }
                if (current != null && current.isArray()) {
                    int idx = Integer.parseInt(idxStr.trim());
                    current = idx < current.size() ? current.get(idx) : null;
                }
            } else {
                current = current.get(seg);
            }
            if (current == null) {
                break;
            }
        }
        return current;
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
