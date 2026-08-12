package com.etl.service.reader;

import com.etl.dto.StepConfig;
import com.etl.entity.DatasourceConfig;
import com.etl.entity.EtlTaskConfig;
import com.etl.enums.HttpAuthType;
import com.etl.service.core.DataSourceManager;
import com.etl.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.Getter;
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
import org.apache.hc.core5.http.Header;
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

    /** 最后一次请求的原始响应信息 */
    @Getter
    private String lastRawResponse;
    @Getter
    private int lastStatusCode;
    @Getter
    private Map<String, String> lastResponseHeaders;
    @Getter
    private String lastRequestUrl;
    @Getter
    private String lastRequestMethod;

    @Override
    public String getSourceType() {
        return "HTTP";
    }

    @Override
    public void init(EtlTaskConfig task, DataSourceManager dataSourceManager) {
        this.taskConfig = task;
        this.dataSourceManager = dataSourceManager;

        // 如果任务未配置 httpUrl，从数据源节点回退读取
        resolveFromDatasourceConfig(task, dataSourceManager);

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
    public void initWithConfig(StepConfig config, DataSourceManager dataSourceManager) {
        EtlTaskConfig task = new EtlTaskConfig();
        task.setSourceDsName(config.getSourceDsName());
        task.setSourceType(config.getSourceType());
        task.setHttpUrl(config.getHttpUrl());
        task.setHttpMethod(config.getHttpMethod());
        task.setHttpHeaders(config.getHttpHeaders());
        task.setHttpBody(config.getHttpBody());
        task.setHttpAuthType(config.getHttpAuthType());
        task.setHttpUsername(config.getHttpUsername());
        task.setHttpPassword(config.getHttpPassword());
        task.setHttpToken(config.getHttpToken());
        task.setHttpResponseType(config.getHttpResponseType());
        task.setHttpDataPath(config.getHttpDataPath());
        task.setHttpPagination(config.getHttpPagination());
        task.setHttpPageParam(config.getHttpPageParam());
        task.setHttpSizeParam(config.getHttpSizeParam());
        task.setHttpPageSize(config.getHttpPageSize());
        task.setHttpTimeout(config.getHttpTimeout());
        task.setHttpEncoding(config.getHttpEncoding());
        task.setHttpMaxRows(config.getHttpMaxRows());
        task.setHttpMaxPages(config.getHttpMaxPages());
        task.setSoapAction(config.getSoapAction());
        task.setSoapBinding(config.getSoapBinding());
        task.setSoapNamespace(config.getSoapNamespace());
        task.setBatchSize(config.getBatchSize());
        task.setTimeoutSeconds(config.getTimeoutSeconds());
        this.init(task, dataSourceManager);
    }

    /**
     * 当任务未独立配置 HTTP 连接参数时，尝试从数据源节点回退填充。
     */
    private void resolveFromDatasourceConfig(EtlTaskConfig task, DataSourceManager dataSourceManager) {
        String dsName = task.getSourceDsName();
        if (dsName == null || dsName.trim().isEmpty()) {
            return;
        }
        DatasourceConfig dsConfig = dataSourceManager.getConfig(dsName);
        if (dsConfig == null) {
            return;
        }
        // 仅当任务未设置时从节点回退 URL
        if (task.getHttpUrl() == null || task.getHttpUrl().trim().isEmpty()) {
            task.setHttpUrl(dsConfig.getJdbcUrl());
        }
        // 回退认证信息
        if (task.getHttpAuthType() == null || "NONE".equals(task.getHttpAuthType())) {
            if (dsConfig.getAuthType() != null && !"NONE".equals(dsConfig.getAuthType())) {
                task.setHttpAuthType(dsConfig.getAuthType());
                if ("BASIC".equals(dsConfig.getAuthType())) {
                    if (task.getHttpUsername() == null || task.getHttpUsername().trim().isEmpty()) {
                        task.setHttpUsername(dsConfig.getUsername());
                    }
                    if (task.getHttpPassword() == null || task.getHttpPassword().trim().isEmpty()) {
                        task.setHttpPassword(dsConfig.getPassword());
                    }
                } else if ("TOKEN".equals(dsConfig.getAuthType())) {
                    if (task.getHttpToken() == null || task.getHttpToken().trim().isEmpty()) {
                        task.setHttpToken(dsConfig.getAuthToken());
                    }
                }
            }
        }
        // 回退超时
        if (task.getHttpTimeout() == null || task.getHttpTimeout() <= 0) {
            task.setHttpTimeout(dsConfig.getTimeout() != null ? dsConfig.getTimeout() : 30000);
        }
    }

    @Override
    public List<Map<String, Object>> readAll() {
        List<Map<String, Object>> allResults = new ArrayList<>();
        int maxRows = taskConfig.getHttpMaxRows() != null && taskConfig.getHttpMaxRows() > 0 ? taskConfig.getHttpMaxRows() : Integer.MAX_VALUE;
        int maxPages = taskConfig.getHttpMaxPages() != null && taskConfig.getHttpMaxPages() > 0 ? taskConfig.getHttpMaxPages() : Integer.MAX_VALUE;

        if ("Y".equals(taskConfig.getHttpPagination())) {
            int pagesFetched = 0;
            while (hasMore && pagesFetched < maxPages) {
                List<Map<String, Object>> batch = fetchPage();
                pagesFetched++;
                if (batch.isEmpty()) {
                    break;
                }
                for (Map<String, Object> row : batch) {
                    allResults.add(row);
                    if (allResults.size() >= maxRows) {
                        log.info("HTTP抽取达到行数上限 {}，停止后续分页 (已抽 {} 页)", maxRows, pagesFetched);
                        hasMore = false;
                        break;
                    }
                }
                if (allResults.size() >= maxRows) break;
            }
            if (pagesFetched >= maxPages) {
                log.info("HTTP抽取达到页数上限 {}，停止后续分页", maxPages);
                hasMore = false;
            }
        } else {
            List<Map<String, Object>> batch = fetchPage();
            for (Map<String, Object> row : batch) {
                allResults.add(row);
                if (allResults.size() >= maxRows) {
                    log.info("HTTP抽取达到行数上限 {}，截断剩余 {} 行", maxRows, batch.size() - allResults.size());
                    break;
                }
            }
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

            // 记录请求信息
            this.lastRequestUrl = url;
            this.lastRequestMethod = method;

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
                // 记录原始响应信息
                this.lastStatusCode = response.getCode();
                this.lastResponseHeaders = new HashMap<>();
                Header[] respHeaders = response.getHeaders();
                if (respHeaders != null) {
                    for (Header h : respHeaders) {
                        lastResponseHeaders.put(h.getName(), h.getValue());
                    }
                }

                HttpEntity entity = response.getEntity();
                String body = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                this.lastRawResponse = body;

                return parseResponse(body);
            }
        } catch (Exception e) {
            log.error("HTTP请求失败", e);
            this.lastRawResponse = null;
            this.lastStatusCode = 0;
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
     * 解析 JSON 格式响应。
     * 当指定了 httpDataPath 时仍需先读整棵树路径解析；未指定 dataPath 时尝试流式解析顶层数组/对象。
     */
    private List<Map<String, Object>> parseJsonResponse(String body) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String dataPath = taskConfig.getHttpDataPath();

        if (dataPath == null || dataPath.isEmpty()) {
            // 流式解析：避免一次性 readTree 把整棵树建到内存
            try (com.fasterxml.jackson.core.JsonParser parser = mapper.getFactory().createParser(body)) {
                com.fasterxml.jackson.core.JsonToken token = parser.nextToken();
                if (token == null) {
                    return Collections.emptyList();
                }
                // 顶层若为数组，则按行流式映射
                if (token == com.fasterxml.jackson.core.JsonToken.START_ARRAY) {
                    com.fasterxml.jackson.databind.MappingIterator<Map<String, Object>> it =
                            mapper.readerFor(Map.class).readValues(parser);
                    List<Map<String, Object>> results = new ArrayList<>();
                    int maxRows = taskConfig.getHttpMaxRows() != null && taskConfig.getHttpMaxRows() > 0 ? taskConfig.getHttpMaxRows() : Integer.MAX_VALUE;
                    while (it.hasNext()) {
                        Map<String, Object> row = it.next();
                        results.add(row);
                        if (results.size() >= maxRows) break;
                    }
                    return results;
                }
                // 顶层若为对象，则返回单行
                if (token == com.fasterxml.jackson.core.JsonToken.START_OBJECT) {
                    Map<String, Object> row = mapper.readValue(parser, Map.class);
                    return Collections.singletonList(row);
                }
                return Collections.emptyList();
            }
        }

        // 带 dataPath：仍需解析到指定节点（小范围 root，影响有限）
        JsonNode root = mapper.readTree(body);
        root = resolveJsonPath(root, dataPath);
        if (root != null && root.isArray()) {
            List<Map<String, Object>> results = new ArrayList<>();
            int maxRows = taskConfig.getHttpMaxRows() != null && taskConfig.getHttpMaxRows() > 0 ? taskConfig.getHttpMaxRows() : Integer.MAX_VALUE;
            for (JsonNode node : root) {
                results.add(JsonUtil.mapToObject(JsonUtil.objectToMap(node), HashMap.class));
                if (results.size() >= maxRows) break;
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
    public boolean supportsStreaming() {
        // HttpReader.readBatch 单页语义明确，可按 chunk 边读边写；不分页时 readAll 即单页，等同一次 chunk
        return true;
    }

    @Override
    public boolean testConnection(DatasourceConfig config, DataSourceManager dataSourceManager) {
        try {
            int timeout = config.getTimeout() != null ? config.getTimeout() : 10000;
            try (CloseableHttpClient client = HttpClients.custom()
                    .setDefaultRequestConfig(RequestConfig.custom()
                            .setConnectTimeout(Timeout.ofMilliseconds(timeout))
                            .setResponseTimeout(Timeout.ofMilliseconds(timeout))
                            .build())
                    .build()) {

                // 使用 GET 测试可达性，即使 API 要求 POST，至少能知道服务是否在线
                HttpGet request = new HttpGet(config.getJdbcUrl());
                try (CloseableHttpResponse response = client.execute(request)) {
                    int code = response.getCode();
                    // 接受 2xx/3xx/4xx 作为可达（405 Method Not Allowed 等说明服务存在）
                    log.info("HTTP连接测试: {} -> 状态码 {}", config.getJdbcUrl(), code);
                    return code > 0;
                }
            }
        } catch (Exception e) {
            log.warn("HTTP连接测试失败: {}", config.getJdbcUrl(), e);
            return false;
        }
    }

    @Override
    public List<Map<String, Object>> preview(int limit) {
        // 真流式预览：不再 readAll 后切片，直接拉一页（必要时按 limit 切），避免 OOM
        if (limit <= 0) limit = 50;
        List<Map<String, Object>> page;
        try {
            // 临时把 pageSize 缩到 limit，避免一页拉几万行只看前 50
            Integer originalSize = taskConfig.getHttpPageSize();
            boolean paged = "Y".equals(taskConfig.getHttpPagination());
            if (paged) {
                taskConfig.setHttpPageSize(Math.min(limit, originalSize != null && originalSize > 0 ? originalSize : limit));
            }
            page = fetchPage();
            if (paged) {
                taskConfig.setHttpPageSize(originalSize);
            }
        } catch (Exception e) {
            log.warn("HTTP预览拉取失败", e);
            return Collections.emptyList();
        }
        return page.size() <= limit ? page : new ArrayList<>(page.subList(0, limit));
    }

    /**
     * 从原始响应体提取结构化 schema（树形），供前端可视化展示。
     * 对数组类型只展开第一个元素的结构，不重复所有元素。
     *
     * @param body 原始响应体文本
     * @return schema 根节点列表
     */
    public List<Map<String, Object>> extractSchema(String body) {
        if (body == null || body.isEmpty()) return Collections.emptyList();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(body);
            List<Map<String, Object>> nodes = new ArrayList<>();
            extractSchemaNodes(root, "", nodes);
            return nodes;
        } catch (Exception e) {
            log.warn("提取schema失败", e);
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private void extractSchemaNodes(JsonNode node, String prefix, List<Map<String, Object>> out) {
        if (node == null || node.isNull()) return;

        if (node.isObject()) {
            // 对象：展开每个字段
            java.util.Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String fieldPath = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
                JsonNode value = entry.getValue();
                Map<String, Object> nodeMap = new LinkedHashMap<>();
                nodeMap.put("path", fieldPath);
                nodeMap.put("type", jsonNodeType(value));
                if (value != null && !value.isArray() && !value.isObject()) {
                    nodeMap.put("sample", sampleValue(value));
                }
                if (value != null && (value.isArray() || value.isObject())) {
                    List<Map<String, Object>> children = new ArrayList<>();
                    if (value.isArray() && value.size() > 0 && value.get(0) != null && !value.get(0).isNull()) {
                        // 数组：只展示第一个元素的结构
                        extractSchemaNodes(value.get(0), fieldPath, children);
                    } else if (value.isObject()) {
                        // 对象：递归展开所有子字段
                        extractSchemaNodes(value, fieldPath, children);
                    }
                    nodeMap.put("children", children);
                }
                out.add(nodeMap);
            }
        } else if (node.isArray()) {
            // 数组根节点：展示元素结构
            Map<String, Object> nodeMap = new LinkedHashMap<>();
            nodeMap.put("path", prefix.isEmpty() ? "$" : prefix);
            nodeMap.put("type", "array");
            nodeMap.put("size", node.size());
            if (node.size() > 0 && node.get(0) != null && !node.get(0).isNull()) {
                List<Map<String, Object>> children = new ArrayList<>();
                extractSchemaNodes(node.get(0), prefix, children);
                nodeMap.put("children", children);
            }
            out.add(nodeMap);
        }
        // 基本类型节点在父节点中已处理，这里不单独输出
    }

    private static String jsonNodeType(JsonNode node) {
        if (node == null || node.isNull()) return "null";
        if (node.isObject()) return "object";
        if (node.isArray()) return "array";
        if (node.isTextual()) return "string";
        if (node.isNumber()) {
            return node.isInt() || node.isLong() ? "number" : "decimal";
        }
        if (node.isBoolean()) return "boolean";
        return "unknown";
    }

    private static Object sampleValue(JsonNode node) {
        if (node == null || node.isNull()) return null;
        if (node.isTextual()) return node.asText();
        if (node.isBoolean()) return node.asBoolean();
        if (node.isNumber()) return node.numberValue();
        return node.asText();
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
