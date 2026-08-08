package com.etl.service.reader;

import com.etl.entity.DatasourceConfig;
import com.etl.entity.EtlTaskConfig;
import com.etl.service.core.DataSourceManager;
import com.etl.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * WebService / SOAP 数据抽取读取器。
 * 支持 SOAP 1.1 和 SOAP 1.2，响应解析支持 JSON 和 XML（含命名空间处理）。
 */
@Slf4j
@Component
public class WebServiceReader implements DataSourceReader {

    private EtlTaskConfig taskConfig;
    private CloseableHttpClient httpClient;

    @Override
    public String getSourceType() {
        return "SOAP";
    }

    @Override
    public void init(EtlTaskConfig task, DataSourceManager dataSourceManager) {
        this.taskConfig = task;
        int timeout = task.getHttpTimeout() != null ? task.getHttpTimeout() : 60000;
        this.httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(Timeout.ofMilliseconds(timeout))
                        .setResponseTimeout(Timeout.ofMilliseconds(timeout))
                        .build())
                .build();
    }

    @Override
    public List<Map<String, Object>> readAll() {
        return executeSoapCall();
    }

    @Override
    public List<Map<String, Object>> readBatch(int batchSize) {
        return executeSoapCall();
    }

    @Override
    public long getTotalCount() {
        return -1;
    }

    @Override
    public boolean testConnection(DatasourceConfig config, DataSourceManager dataSourceManager) {
        // 对于 SOAP 连接，简单测试 endpoint 可达性
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(config.getJdbcUrl());
            try (CloseableHttpResponse response = client.execute(request)) {
                return response.getCode() < 500;
            }
        } catch (Exception e) {
            log.warn("SOAP连接测试失败: {}", config.getJdbcUrl(), e);
            return false;
        }
    }

    @Override
    public List<Map<String, Object>> preview(int limit) {
        List<Map<String, Object>> all = executeSoapCall();
        if (all.size() > limit) {
            return all.subList(0, limit);
        }
        return all;
    }

    @Override
    public void close() {
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (Exception e) {
                log.warn("关闭SOAP客户端失败", e);
            }
        }
    }

    /**
     * 执行 SOAP 调用并解析响应
     */
    private List<Map<String, Object>> executeSoapCall() {
        String url = taskConfig.getHttpUrl();
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("SOAP endpoint URL 为空");
        }

        String soapBody = taskConfig.getHttpBody();       // SOAP Envelope (XML)
        String soapAction = taskConfig.getSoapAction();
        String binding = taskConfig.getSoapBinding();
        if (binding == null || binding.isEmpty()) {
            binding = "SOAP11";
        }
        if (soapBody == null || soapBody.trim().isEmpty()) {
            throw new IllegalArgumentException("SOAP Envelope(Body) 为空");
        }

        boolean isSoap12 = "SOAP12".equalsIgnoreCase(binding);
        String contentType = isSoap12 ? "application/soap+xml; charset=UTF-8" : "text/xml; charset=UTF-8";

        try {
            HttpPost request = new HttpPost(url);
            request.setHeader("Content-Type", contentType);
            if (soapAction != null && !soapAction.trim().isEmpty()) {
                if (isSoap12) {
                    request.setHeader("SOAPAction", soapAction);
                } else {
                    request.setHeader("SOAPAction", "\"" + soapAction + "\"");
                }
            }
            request.setEntity(new StringEntity(soapBody, ContentType.create(contentType, StandardCharsets.UTF_8)));

            // 设置额外的自定义请求头（如果有）
            String headersStr = taskConfig.getHttpHeaders();
            if (headersStr != null && !headersStr.isEmpty()) {
                try {
                    Map<String, String> headers = JsonUtil.fromJson(headersStr, HashMap.class);
                    headers.forEach(request::setHeader);
                } catch (Exception e) {
                    log.warn("解析自定义请求头失败", e);
                }
            }

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();
                String body = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                log.info("SOAP响应收到, 长度: {}", body.length());
                return parseSoapResponse(body);
            }
        } catch (Exception e) {
            log.error("SOAP调用失败", e);
            throw new RuntimeException("SOAP调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析 SOAP 响应
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseSoapResponse(String body) {
        String responseType = taskConfig.getHttpResponseType();
        if (responseType == null) responseType = "XML";

        try {
            if ("JSON".equalsIgnoreCase(responseType)) {
                return parseJsonResponse(body);
            } else {
                return parseXmlResponse(body);
            }
        } catch (Exception e) {
            log.error("解析SOAP响应失败", e);
            throw new RuntimeException("解析SOAP响应失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析 JSON 格式的 SOAP 响应
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
     * 解析 XML 格式的 SOAP 响应
     */
    private List<Map<String, Object>> parseXmlResponse(String body) throws Exception {
        XmlMapper xmlMapper = new XmlMapper();

        // 先解析为 JSON Node，方便用 JSONPath 提取数据
        JsonNode root = xmlMapper.readTree(body.getBytes(StandardCharsets.UTF_8));

        // SOAP 响应有固定的 Envelope → Body 包装，自动跳过
        // 尝试跳过 soap:Body 层级
        JsonNode bodyNode = findSoapBody(root);
        if (bodyNode != null) {
            root = bodyNode;
        }

        // 使用 dataPath 进一步定位
        String dataPath = taskConfig.getHttpDataPath();
        if (dataPath != null && !dataPath.isEmpty()) {
            root = resolveJsonPath(root, dataPath);
        }

        if (root != null && root.isArray()) {
            List<Map<String, Object>> results = new ArrayList<>();
            for (JsonNode node : root) {
                results.add(jsonNodeToMap(node));
            }
            return results;
        }

        // 单个对象包装为数组
        if (root != null && root.isObject()) {
            List<Map<String, Object>> results = new ArrayList<>();
            results.add(jsonNodeToMap(root));
            return results;
        }

        return Collections.emptyList();
    }

    /**
     * 遍历 XML DOM 寻找 SOAP Body 内的实际数据节点
     */
    private JsonNode findSoapBody(JsonNode root) {
        // 尝试常见 SOAP Body 路径
        String[] bodyPaths = {"Body", "Envelope", "soap:Body", "soap:Envelope",
                "SOAP-ENV:Body", "SOAP-ENV:Envelope"};
        for (String path : bodyPaths) {
            JsonNode n = root.get(path);
            if (n != null && !n.isMissingNode()) {
                // 尝试继续向下找到实际数据
                if (n.size() > 0) {
                    JsonNode inner = findSoapBody(n);
                    return inner != null ? inner : n;
                }
                return n;
            }
        }
        // 遍历所有字段寻找非 SOAP 包装的实际数据
        if (root.isObject()) {
            java.util.Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String key = entry.getKey();
                if (!key.contains("soap") && !key.contains("SOAP") && !key.contains("Envelope")) {
                    return findSoapBody(entry.getValue());
                }
            }
        }
        return root;
    }

    private JsonNode resolveJsonPath(JsonNode root, String path) {
        String p = path.trim();
        if (p.startsWith("$")) p = p.substring(1);
        if (p.startsWith(".")) p = p.substring(1);
        if (p.isEmpty()) return root;

        JsonNode current = root;
        String[] segments = p.split("\\.");
        for (String seg : segments) {
            if (seg.isEmpty() || current == null) continue;
            int bracket = seg.indexOf('[');
            if (bracket >= 0) {
                String field = seg.substring(0, bracket);
                String idxStr = seg.substring(bracket + 1, seg.indexOf(']'));
                if (!field.isEmpty()) current = current.get(field);
                if (current != null && current.isArray()) {
                    int idx = Integer.parseInt(idxStr.trim());
                    current = idx < current.size() ? current.get(idx) : null;
                }
            } else {
                current = current.get(seg);
            }
            if (current == null) break;
        }
        return current;
    }

    private Map<String, Object> jsonNodeToMap(JsonNode node) {
        if (node == null) return new HashMap<>();
        return JsonUtil.mapToObject(JsonUtil.objectToMap(node), HashMap.class);
    }
}
