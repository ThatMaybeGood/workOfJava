package com.reports.etl.service.extractor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.reports.etl.entity.EtlTask;
import com.reports.etl.entity.EtlWsConfig;
import com.reports.etl.mapper.EtlTaskMapper;
import com.reports.etl.mapper.EtlWsConfigMapper;
import com.reports.etl.service.registry.EtlDataSourceRegistry;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.Duration;
import java.util.*;

@Slf4j
@Component
public class WebServiceExtractor {

    private final EtlWsConfigMapper wsConfigMapper;
    private final EtlTaskMapper taskMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ObjectMapper xmlMapper = new XmlMapper();
    private final EtlDataSourceRegistry registry;

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(30))
            .readTimeout(Duration.ofSeconds(120))
            .writeTimeout(Duration.ofSeconds(30))
            .build();

    public WebServiceExtractor(EtlWsConfigMapper wsConfigMapper, EtlTaskMapper taskMapper,
                               EtlDataSourceRegistry registry) {
        this.wsConfigMapper = wsConfigMapper;
        this.taskMapper = taskMapper;
        this.registry = registry;
    }

    @PostConstruct
    public void init() {
        log.info("WebServiceExtractor 初始化完成");
    }

    public Map<String, Object> extract(Long taskId, int batchSize) {
        EtlWsConfig wsConfig = wsConfigMapper.selectById(taskId);
        if (wsConfig == null) throw new RuntimeException("WebService配置不存在: " + taskId);

        EtlTask task = findTask(taskId);
        String responsePath = wsConfig.getResponsePath();
        int maxPages = wsConfig.getMaxPages() != null ? wsConfig.getMaxPages() : 10;
        int maxRows = wsConfig.getMaxRows() != null ? wsConfig.getMaxRows() : 10000;

        List<Map<String, Object>> allRows = new ArrayList<>();
        String bodyTemplate = wsConfig.getRequestBodyTemplate();
        String headersJson = wsConfig.getHeadersJson();
        Map<String, String> headers = parseJsonMap(headersJson);

        // 分页拉取
        for (int page = 1; page <= maxPages && allRows.size() < maxRows; page++) {
            String body = buildRequestBody(bodyTemplate, page, wsConfig.getExtractParamsJson());
            Request.Builder reqBuilder = new Request.Builder().url(wsConfig.getUrl());
            headers.forEach(reqBuilder::addHeader);
            if (wsConfig.getWsType() != null && "SOAP".equals(wsConfig.getWsType())) {
                reqBuilder.addHeader("Content-Type", "text/xml; charset=utf-8");
                if (wsConfig.getSoapAction() != null) {
                    reqBuilder.addHeader("SOAPAction", wsConfig.getSoapAction());
                }
            }
            Request request = reqBuilder.post(RequestBody.create(MediaType.parse("application/json"), body)).build();

            try (Response response = CLIENT.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("WebService调用失败: HTTP " + response.code());
                }
                String responseBody = response.body() != null ? response.body().string() : "";

                // 解析响应
                JsonNode node = parseResponse(responseBody, wsConfig.getWsType());
                JsonNode data = responsePath != null && !responsePath.isEmpty()
                        ? navigate(node, responsePath)
                        : node;

                if (data == null || data.isNull()) {
                    break;
                }

                // 如果是数组，展开；否则包装成单元素
                if (data.isArray()) {
                    for (JsonNode item : data) {
                        allRows.add(flatten(item, ""));
                        if (allRows.size() >= maxRows) break;
                    }
                } else if (data.isObject()) {
                    allRows.add(flatten(data, ""));
                }

            } catch (IOException e) {
                throw new RuntimeException("WebService响应读取失败", e);
            }
        }

        List<Map<String, Object>> batch = allRows.subList(0, Math.min(batchSize, allRows.size()));
        boolean more = allRows.size() > batchSize;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rows", batch);
        result.put("totalRows", allRows.size());
        result.put("more", more);
        result.put("columns", batch.isEmpty() ? new ArrayList<>() : new ArrayList<>(batch.get(0).keySet()));
        return result;
    }

    private EtlTask findTask(Long taskId) {
        return taskMapper.selectById(taskId);
    }

    private String buildRequestBody(String template, int page, String paramsJson) {
        if (template == null || template.isEmpty()) return "{}";
        // 简单占位符替换
        template = template.replace("{page}", String.valueOf(page));
        template = template.replace("{pageSize}", "500");
        return template;
    }

    private Map<String, String> parseJsonMap(String json) {
        if (json == null || json.isEmpty()) return Collections.emptyMap();
        try {
            JsonNode node = objectMapper.readTree(json);
            Map<String, String> map = new LinkedHashMap<>();
            node.fields().forEachRemaining(e -> map.put(e.getKey(), e.getValue().asText()));
            return map;
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private JsonNode parseResponse(String body, String wsType) {
        try {
            if ("SOAP".equals(wsType)) {
                Object xmlObj = xmlMapper.readValue(body, Object.class);
                return objectMapper.convertValue(xmlObj, JsonNode.class);
            }
            return objectMapper.readTree(body);
        } catch (Exception e) {
            throw new RuntimeException("解析WebService响应失败: " + e.getMessage(), e);
        }
    }

    private JsonNode navigate(JsonNode root, String path) {
        if (root == null) return null;
        String[] parts = path.split("\\.");
        JsonNode current = root;
        for (String part : parts) {
            if (current == null || current.isMissingNode()) return null;
            current = current.get(part);
        }
        return current;
    }

    private Map<String, Object> flatten(JsonNode node, String prefix) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (node.isObject()) {
            node.fields().forEachRemaining(e -> {
                String key = prefix.isEmpty() ? e.getKey() : prefix + "." + e.getKey();
                if (e.getValue().isObject() || e.getValue().isArray()) {
                    result.putAll(flatten(e.getValue(), key));
                } else {
                    result.put(key, e.getValue().isMissingNode() ? null : stringifyNode(e.getValue()));
                }
            });
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                String key = prefix + "[" + i + "]";
                if (node.get(i).isObject() || node.get(i).isArray()) {
                    result.putAll(flatten(node.get(i), key));
                } else {
                    result.put(key, node.get(i).isMissingNode() ? null : stringifyNode(node.get(i)));
                }
            }
        } else {
            result.put(prefix, node.asText());
        }
        return result;
    }

    private String stringifyNode(JsonNode node) {
        if (node == null || node.isNull()) return null;
        if (node.isTextual()) return node.asText();
        if (node.isNumber()) return node.asText();
        if (node.isBoolean()) return String.valueOf(node.asBoolean());
        return node.toString();
    }
}