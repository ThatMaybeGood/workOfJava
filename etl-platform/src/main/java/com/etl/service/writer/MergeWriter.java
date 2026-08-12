package com.etl.service.writer;

import com.etl.dto.StepConfig;
import com.etl.entity.EtlColumnMapping;
import com.etl.entity.EtlStepColumnMapping;
import com.etl.entity.EtlTaskConfig;
import com.etl.service.core.DataSourceManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MergeWriter implements DataWriter {

    @Override
    public String getWriteMode() {
        return "MERGE";
    }

    @Override
    public void write(List<Map<String, Object>> data, EtlTaskConfig task,
                      List<EtlColumnMapping> mappings, DataSourceManager dataSourceManager) {
        if (data == null || data.isEmpty()) {
            return;
        }

        JdbcTemplate jdbcTemplate = dataSourceManager.getJdbcTemplate(task.getTargetDsName());
        int batchSize = task.getBatchSize() != null ? task.getBatchSize() : 2000;

        for (int i = 0; i < data.size(); i += batchSize) {
            List<Map<String, Object>> batch = data.subList(i, Math.min(i + batchSize, data.size()));
            mergeBatch(jdbcTemplate, batch, task, mappings);
            log.info("MERGE批次 {}/{}, 处理 {} 条数据", (i / batchSize) + 1,
                    (data.size() + batchSize - 1) / batchSize, batch.size());
        }
    }

    @Override
    public void writeWithConfig(List<Map<String, Object>> data, StepConfig config,
                                 List<EtlStepColumnMapping> mappings, DataSourceManager dataSourceManager) {
        EtlTaskConfig task = new EtlTaskConfig();
        task.setTargetDsName(config.getTargetDsName());
        task.setTargetTable(config.getTargetTable());
        task.setWriteMode(config.getWriteMode());
        task.setBatchSize(config.getBatchSize());
        List<EtlColumnMapping> oldMappings = convertMappings(mappings);
        this.write(data, task, oldMappings, dataSourceManager);
    }

    private List<EtlColumnMapping> convertMappings(List<EtlStepColumnMapping> stepMappings) {
        if (stepMappings == null) return null;
        List<EtlColumnMapping> list = new ArrayList<>();
        for (EtlStepColumnMapping sm : stepMappings) {
            EtlColumnMapping m = new EtlColumnMapping();
            m.setSourceColumn(sm.getSourceColumn());
            m.setTargetColumn(sm.getTargetColumn());
            m.setDataType(sm.getDataType());
            m.setDefaultValue(sm.getDefaultValue());
            m.setTransformExpr(sm.getTransformExpr());
            m.setMappingOrder(sm.getMappingOrder());
            m.setIsPrimaryKey(sm.getIsPrimaryKey());
            m.setEnabled(sm.getEnabled());
            list.add(m);
        }
        return list;
    }

    private void mergeBatch(JdbcTemplate jdbcTemplate, List<Map<String, Object>> batch,
                            EtlTaskConfig task, List<EtlColumnMapping> mappings) {
        if (batch.isEmpty()) {
            return;
        }

        List<String> targetColumns = getTargetColumns(mappings, batch.get(0));
        List<String> primaryKeys = getPrimaryKeys(mappings);
        if (primaryKeys.isEmpty()) {
            throw new IllegalArgumentException("MERGE 写入模式必须至少配置一个主键字段（isPrimaryKey=Y）用于匹配");
        }

        // Oracle MERGE INTO 语法
        String mergeSql = buildMergeSql(task.getTargetTable(), targetColumns, primaryKeys);

        List<Object[]> batchArgs = new ArrayList<>();
        for (Map<String, Object> row : batch) {
            Object[] args = new Object[targetColumns.size() + targetColumns.size()];
            int idx = 0;
            // VALUES部分（INSERT用）
            for (String col : targetColumns) {
                args[idx++] = getValue(row, col, mappings);
            }
            // UPDATE SET部分
            for (String col : targetColumns) {
                args[idx++] = getValue(row, col, mappings);
            }
            batchArgs.add(args);
        }

        jdbcTemplate.batchUpdate(mergeSql, batchArgs);
    }

    private String buildMergeSql(String tableName, List<String> columns, List<String> primaryKeys) {
        StringBuilder sb = new StringBuilder();
        sb.append("MERGE INTO ").append(tableName).append(" t USING (SELECT ");

        // SELECT 参数
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("? AS ").append(columns.get(i));
        }
        sb.append(" FROM DUAL) s ON (");

        // ON 条件（主键匹配）
        for (int i = 0; i < primaryKeys.size(); i++) {
            if (i > 0) sb.append(" AND ");
            sb.append("t.").append(primaryKeys.get(i)).append(" = s.").append(primaryKeys.get(i));
        }
        sb.append(") ");

        // WHEN MATCHED THEN UPDATE
        sb.append("WHEN MATCHED THEN UPDATE SET ");
        List<String> nonPkColumns = columns.stream()
                .filter(c -> !primaryKeys.contains(c))
                .collect(Collectors.toList());
        for (int i = 0; i < nonPkColumns.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("t.").append(nonPkColumns.get(i)).append(" = s.").append(nonPkColumns.get(i));
        }

        // WHEN NOT MATCHED THEN INSERT
        sb.append(" WHEN NOT MATCHED THEN INSERT (");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(columns.get(i));
        }
        sb.append(") VALUES (");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("s.").append(columns.get(i));
        }
        sb.append(")");

        return sb.toString();
    }

    private List<String> getTargetColumns(List<EtlColumnMapping> mappings, Map<String, Object> row) {
        List<String> columns = new ArrayList<>();
        if (mappings != null && !mappings.isEmpty()) {
            for (EtlColumnMapping mapping : mappings) {
                if ("Y".equals(mapping.getEnabled())) {
                    columns.add(mapping.getTargetColumn());
                }
            }
        } else {
            columns.addAll(row.keySet());
        }
        return columns;
    }

    private List<String> getPrimaryKeys(List<EtlColumnMapping> mappings) {
        if (mappings == null) return new ArrayList<>();
        return mappings.stream()
                .filter(m -> "Y".equals(m.getIsPrimaryKey()))
                .map(EtlColumnMapping::getTargetColumn)
                .collect(Collectors.toList());
    }

    private Object getValue(Map<String, Object> row, String targetCol, List<EtlColumnMapping> mappings) {
        if (mappings != null) {
            for (EtlColumnMapping m : mappings) {
                if (m.getTargetColumn().equalsIgnoreCase(targetCol)) {
                    Object value = resolveDotPath(row, m.getSourceColumn());
                    if (value == null && m.getDefaultValue() != null) {
                        return m.getDefaultValue();
                    }
                    return value;
                }
            }
        }
        return resolveDotPath(row, targetCol);
    }

    /**
     * 按 dot-path 从 map 中取值，支持嵌套对象和数组下标（与 InsertWriter 保持一致）。
     */
    @SuppressWarnings("unchecked")
    private Object resolveDotPath(Map<String, Object> root, String path) {
        if (path == null || path.isEmpty() || root == null) return null;
        Object current = root;
        for (String segment : path.split("\\.")) {
            if (current == null) return null;
            if (segment.startsWith("[") || segment.equals("*")) continue;
            if (!(current instanceof Map)) return null;
            current = ((Map<String, Object>) current).get(segment);
        }
        return current;
    }
}
