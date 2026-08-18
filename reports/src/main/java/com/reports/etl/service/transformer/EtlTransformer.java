package com.reports.etl.service.transformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.etl.entity.EtlMapping;
import com.reports.etl.service.core.EtlMetaDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
public class EtlTransformer {

    private final EtlMetaDao metaDao;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EtlTransformer(EtlMetaDao metaDao) {
        this.metaDao = metaDao;
    }

    @PostConstruct
    public void init() {
        log.info("EtlTransformer 初始化完成");
    }

    public List<Map<String, Object>> transform(Long taskId, List<Map<String, Object>> rows) {
        List<EtlMapping> mappings = metaDao.listMappings(taskId);

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

    private static final java.time.format.DateTimeFormatter TS_FMT =
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Object convertType(Object value, String tgtType) {
        if (value == null) return null;
        if (tgtType == null) return value;
        String type = tgtType.toLowerCase();
        String str = value.toString().trim();
        try {
            if (type.contains("int") || type.contains("long")) {
                return Long.parseLong(str);
            } else if (type.contains("double") || type.contains("float") || type.contains("decimal") || type.contains("number")) {
                return Double.parseDouble(str);
            } else if (type.contains("timestamp") || type.contains("datetime")) {
                if (value instanceof LocalDateTime) return value;
                return LocalDateTime.parse(str.length() == 10 ? str + " 00:00:00" : str, TS_FMT);
            } else if (type.contains("date")) {
                if (value instanceof LocalDateTime) return value;
                return LocalDateTime.parse(str.length() == 10 ? str + " 00:00:00" : str, TS_FMT);
            }
        } catch (Exception e) {
            log.warn("类型转换失败，保留原值: {} -> {}", value, tgtType);
        }
        return value;
    }

    public Map<String, Object> preview(Long taskId, List<Map<String, Object>> sampleRows) {
        List<EtlMapping> mappings = metaDao.listMappings(taskId);
        // 与正式 transform 保持完全一致（含默认值填充与类型转换）
        List<Map<String, Object>> previewRows = transform(taskId, sampleRows);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("mappings", mappings);
        result.put("sampleRows", previewRows);
        return result;
    }
}