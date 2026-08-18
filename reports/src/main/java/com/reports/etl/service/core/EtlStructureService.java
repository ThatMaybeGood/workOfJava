package com.reports.etl.service.core;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 来源结构树分析：
 * 输入一批样例行（未拍平的嵌套结构），递归展开层级；
 * 数组合并所有元素的字段集合为单一子树（isList=true，sampleCount=元素总数）；
 * 标量叶子类型由样例值推断（string/number/boolean/date/null）。
 */
@Service
public class EtlStructureService {

    private static final Pattern DATE_PATTERN = Pattern.compile(
            "^\\d{4}-\\d{2}-\\d{2}([ T]\\d{2}:\\d{2}(:\\d{2})?([.]\\d+)?)?$");

    /**
     * WS：基于原始 JsonNode 样例行构建层级树
     */
    public List<Map<String, Object>> buildTreeFromJson(List<JsonNode> samples) {
        return buildChildren(samples != null ? samples : new ArrayList<JsonNode>(), "");
    }

    /**
     * PROC：基于游标 ResultSetMetaData 列元数据构建平铺树（每列一个节点）
     * 入参 [{name, jdbcType, typeName}]
     */
    public List<Map<String, Object>> buildFlatTree(List<Map<String, Object>> columnsMeta) {
        List<Map<String, Object>> tree = new ArrayList<>();
        if (columnsMeta == null) return tree;
        for (Map<String, Object> col : columnsMeta) {
            String name = String.valueOf(col.get("name"));
            int jdbcType = col.get("jdbcType") != null
                    ? Integer.parseInt(col.get("jdbcType").toString()) : Types.VARCHAR;
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("path", name);
            node.put("name", name);
            node.put("type", mapJdbcType(jdbcType));
            node.put("isList", false);
            node.put("sampleCount", 0);
            node.put("children", new ArrayList<>());
            tree.add(node);
        }
        return tree;
    }

    // ==================== 内部实现 ====================

    private List<Map<String, Object>> buildChildren(List<JsonNode> objects, String parentPath) {
        // 合并所有样例对象的字段集合（LinkedHashMap 保序）
        Map<String, List<JsonNode>> fieldValues = new LinkedHashMap<>();
        for (JsonNode obj : objects) {
            if (obj == null || !obj.isObject()) continue;
            obj.fields().forEachRemaining(e -> {
                List<JsonNode> list = fieldValues.get(e.getKey());
                if (list == null) {
                    list = new ArrayList<>();
                    fieldValues.put(e.getKey(), list);
                }
                list.add(e.getValue());
            });
        }
        List<Map<String, Object>> children = new ArrayList<>();
        for (Map.Entry<String, List<JsonNode>> e : fieldValues.entrySet()) {
            String path = parentPath.isEmpty() ? e.getKey() : parentPath + "." + e.getKey();
            children.add(buildNode(path, e.getKey(), e.getValue()));
        }
        return children;
    }

    private Map<String, Object> buildNode(String path, String name, List<JsonNode> values) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("path", path);
        node.put("name", name);

        boolean anyObject = false;
        boolean anyArray = false;
        for (JsonNode v : values) {
            if (v == null) continue;
            if (v.isObject()) anyObject = true;
            else if (v.isArray()) anyArray = true;
        }

        if (anyArray) {
            // 数组合并：所有元素的字段集合合并为单一子树，不逐元素展开
            List<JsonNode> elements = new ArrayList<>();
            int elementCount = 0;
            for (JsonNode v : values) {
                if (v != null && v.isArray()) {
                    elementCount += v.size();
                    for (JsonNode el : v) elements.add(el);
                }
            }
            node.put("isList", true);
            node.put("sampleCount", elementCount);
            List<Map<String, Object>> children = buildChildren(elements, path);
            node.put("children", children);
            // 元素为对象时 type=object；否则取元素标量推断
            node.put("type", !children.isEmpty() ? "object" : inferScalarType(elements));
        } else if (anyObject) {
            node.put("type", "object");
            node.put("isList", false);
            node.put("sampleCount", values.size());
            node.put("children", buildChildren(values, path));
        } else {
            node.put("type", inferScalarType(values));
            node.put("isList", false);
            node.put("sampleCount", values.size());
            node.put("children", new ArrayList<>());
        }
        return node;
    }

    /**
     * 标量类型推断：string/number/boolean/date/null（跳过 null 值，全 null 则 null）
     */
    private String inferScalarType(List<JsonNode> values) {
        String result = null;
        for (JsonNode v : values) {
            if (v == null || v.isNull() || v.isMissingNode()) continue;
            String t;
            if (v.isNumber()) {
                t = "number";
            } else if (v.isBoolean()) {
                t = "boolean";
            } else if (v.isTextual()) {
                t = DATE_PATTERN.matcher(v.asText()).matches() ? "date" : "string";
            } else {
                t = "string";
            }
            if (result == null) {
                result = t;
            } else if (!result.equals(t)) {
                // 类型冲突时保守取 string
                return "string";
            }
        }
        return result != null ? result : "null";
    }

    private String mapJdbcType(int jdbcType) {
        switch (jdbcType) {
            case Types.INTEGER:
            case Types.BIGINT:
            case Types.SMALLINT:
            case Types.TINYINT:
            case Types.NUMERIC:
            case Types.DECIMAL:
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.REAL:
                return "number";
            case Types.BOOLEAN:
            case Types.BIT:
                return "boolean";
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                return "date";
            case Types.NULL:
                return "null";
            default:
                return "string";
        }
    }
}
