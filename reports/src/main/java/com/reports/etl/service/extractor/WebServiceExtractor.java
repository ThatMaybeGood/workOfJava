package com.reports.etl.service.extractor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.reports.etl.entity.EtlWsConfig;
import com.reports.etl.service.core.EtlMetaDao;
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

    private final EtlMetaDao metaDao;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ObjectMapper xmlMapper = new XmlMapper();

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(30))
            .readTimeout(Duration.ofSeconds(120))
            .writeTimeout(Duration.ofSeconds(30))
            .build();

    public WebServiceExtractor(EtlMetaDao metaDao) {
        this.metaDao = metaDao;
    }

    @PostConstruct
    public void init() {
        log.info("WebServiceExtractor 初始化完成");
    }

    public Map<String, Object> extract(Long taskId, int batchSize) {
        EtlWsConfig wsConfig = metaDao.getWsConfig(taskId);
        if (wsConfig == null) throw new RuntimeException("WebService配置不存在: " + taskId);
        return extract(wsConfig, batchSize);
    }

    /**
     * 以配置对象为入参的抽取（供 SourceExtractorFacade 复用）
     */
    public Map<String, Object> extract(EtlWsConfig wsConfig, int batchSize) {
        int maxPages = wsConfig.getMaxPages() != null ? wsConfig.getMaxPages() : 10;
        int maxRows = wsConfig.getMaxRows() != null ? wsConfig.getMaxRows() : metaDao.getGlobalInt("defaultMaxRows", 10000);

        List<Map<String, Object>> allRows = new ArrayList<>();

        // 分页拉取
        for (int page = 1; page <= maxPages && allRows.size() < maxRows; page++) {
            JsonNode data = fetchPageData(wsConfig, page);
            if (data == null || data.isNull()) {
                break;
            }

            // 数组展开 / 对象包装
            if (data.isArray()) {
                for (JsonNode item : data) {
                    allRows.add(flatten(item, ""));
                    if (allRows.size() >= maxRows) break;
                }
            } else if (data.isObject()) {
                allRows.add(flatten(data, ""));
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

    /**
     * 拉取未拍平的原始样例行（供结构树分析：保留嵌套层级，不走 flatten）
     */
    public List<JsonNode> fetchRawSample(EtlWsConfig wsConfig, int maxRows) {
        int maxPages = wsConfig.getMaxPages() != null ? wsConfig.getMaxPages() : 10;
        List<JsonNode> items = new ArrayList<>();
        for (int page = 1; page <= maxPages && items.size() < maxRows; page++) {
            JsonNode data = fetchPageData(wsConfig, page);
            if (data == null || data.isNull()) {
                break;
            }
            if (data.isArray()) {
                for (JsonNode item : data) {
                    items.add(item);
                    if (items.size() >= maxRows) break;
                }
            } else if (data.isObject()) {
                items.add(data);
            } else {
                break;
            }
        }
        return items;
    }

    /**
     * 请求一页并定位出参节点（复用 SOAP/REST 解析与 responsePath 导航）
     */
    private JsonNode fetchPageData(EtlWsConfig wsConfig, int page) {
        String bodyTemplate = wsConfig.getRequestBodyTemplate();
        Map<String, String> headers = parseJsonMap(wsConfig.getHeadersJson());
        boolean isSoap = wsConfig.getWsType() != null && "SOAP".equals(wsConfig.getWsType());
        String responsePath = wsConfig.getResponsePath();

        String body = buildRequestBody(bodyTemplate, page, wsConfig.getExtractParamsJson());
        Request.Builder reqBuilder = new Request.Builder().url(wsConfig.getUrl());
        headers.forEach(reqBuilder::addHeader);
        if (isSoap) {
            reqBuilder.addHeader("Content-Type", "text/xml; charset=utf-8");
            if (wsConfig.getSoapAction() != null && !wsConfig.getSoapAction().isEmpty()) {
                reqBuilder.addHeader("SOAPAction", wsConfig.getSoapAction());
            }
        }
        // REST 且无请求体模板时按 GET 抽取（多数查询型接口只接受 GET，POST 会误创建资源）；
        // SOAP 或带 body 模板的 REST 仍走 POST
        boolean useGet = !isSoap && (bodyTemplate == null || bodyTemplate.isEmpty());
        Request request;
        if (useGet) {
            request = reqBuilder.get().build();
        } else {
            request = reqBuilder.post(RequestBody.create(MediaType.parse(
                    isSoap ? "text/xml; charset=utf-8" : "application/json"), body)).build();
        }

        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("WebService调用失败: HTTP " + response.code());
            }
            String responseBody = response.body() != null ? response.body().string() : "";

            // 解析响应
            JsonNode node = parseResponse(responseBody, wsConfig.getWsType());
            return responsePath != null && !responsePath.isEmpty()
                    ? navigate(node, responsePath)
                    : node;
        } catch (IOException e) {
            throw new RuntimeException("WebService响应读取失败", e);
        }
    }

    private String buildRequestBody(String template, int page, String paramsJson) {
        if (template == null || template.isEmpty()) return "{}";
        // 简单占位符替换
        template = template.replace("{page}", String.valueOf(page));
        template = template.replace("{pageSize}", "500");
        // 增量占位符
        template = template.replace("{lastTime}", "");
        template = template.replace("{today}", "");
        template = template.replace("{lastRunTime}", "");
        if (paramsJson != null && !paramsJson.isEmpty()) {
            // 允许模板引用参数 json 字段，例如 {paramName}
            // 简单实现：不做展开
        }
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