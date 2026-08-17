package com.reports.etl.service.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.etl.entity.EtlMapping;
import com.reports.etl.mapper.EtlMappingMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component
public class EtlTransformer {

    private final EtlMappingMapper mappingMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EtlTransformer(EtlMappingMapper mappingMapper) {
        this.mappingMapper = mappingMapper;
    }

    @PostConstruct
    public void init() {
        log.info("EtlTransformer 初始化完成");
    }

    public List<Map<String, Object>> transform(Long taskId, List<Map<String, Object>> rows) {
        List<EtlMapping> mappings = mappingMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<EtlMapping>()
                        .eq(EtlMapping::getTaskId, taskId)
                        .orderByAsc(EtlMapping::getSortOrder)
        );

        if (mappings == null || mappings.isEmpty()) {
            return rows; // 无映射则直接返回
        }

        // 按目标列聚合
        Map<String, EtlMapping> tgtFieldMap = new LinkedHashMap<>();
        for (EtlMapping m : mappings) {
            tgtFieldMap.put(m.getTgtField(), m);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> transformed = new LinkedHashMap<>();
            for (Map.Entry<String, EtlMapping> entry : tgtFieldMap.entrySet()) {
                String tgtField = entry.getKey();
                EtlMapping mapping = entry.getValue();
                Object value = extractValue(row, mapping.getSrcField());
                if (value == null && mapping.getDefaultValue() != null) {
                    value = mapping.getDefaultValue();
                }
                transformed.put(tgtField, convertType(value, mapping.getTgtType()));
            }
            result.add(transformed);
        }
        return result;
    }

    private Object extractValue(Map<String, Object> row, String srcField) {
        if (srcField == null || srcField.isEmpty()) return null;
        // dot-path 取值
        String[] parts = srcField.split("\\.");
        Object current = row;
        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else {
                return null;
            }
            if (current == null) return null;
        }
        return current;
    }

    private Object convertType(Object value, String tgtType) {
        if (value == null) return null;
        if (tgtType == null) return value;
        String type = tgtType.toLowerCase();
        try {
            if (type.contains("int") || type.contains("long")) {
                return Long.parseLong(value.toString());
            } else if (type.contains("double") || type.contains("float") || type.contains("decimal")) {
                return Double.parseDouble(value.toString());
            } else if (type.contains("date")) {
                return LocalDateTime.now(); // 简化处理
            }
        } catch (Exception e) {
            log.warn("类型转换失败: {} -> {}", value, tgtType, e);
        }
        return value;
    }

    public Map<String, Object> preview(Long taskId, List<Map<String, Object>> sampleRows) {
        List<EtlMapping> mappings = mappingMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<EtlMapping>()
                        .eq(EtlMapping::getTaskId, taskId)
                        .orderByAsc(EtlMapping::getSortOrder)
        );

        List<Map<String, Object>> previewRows = new ArrayList<>();
        for (Map<String, Object> row : sampleRows) {
            Map<String, Object> transformed = new LinkedHashMap<>();
            for (EtlMapping m : mappings) {
                Object value = extractValue(row, m.getSrcField());
                transformed.put(m.getTgtField(), value);
            }
            previewRows.add(transformed);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("mappings", mappings);
        result.put("sampleRows", previewRows);
        return result;
    }
}