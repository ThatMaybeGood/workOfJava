package com.etl.service.writer;

import com.etl.entity.EtlColumnMapping;
import com.etl.entity.EtlTaskConfig;
import com.etl.service.core.DataSourceManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InsertWriter implements DataWriter {

    @Override
    public String getWriteMode() {
        return "INSERT";
    }

    @Override
    public void write(List<Map<String, Object>> data, EtlTaskConfig task,
                      List<EtlColumnMapping> mappings, DataSourceManager dataSourceManager) {
        if (data == null || data.isEmpty()) {
            return;
        }

        JdbcTemplate jdbcTemplate = dataSourceManager.getJdbcTemplate(task.getTargetDsName());

        // 先清空表（如果配置）
        if ("Y".equals(task.getTruncateBefore())) {
            String truncateSql = "TRUNCATE TABLE " + task.getTargetTable();
            jdbcTemplate.execute(truncateSql);
            log.info("表 [{}] 已清空", task.getTargetTable());
        }

        int batchSize = task.getBatchSize() != null ? task.getBatchSize() : 2000;

        for (int i = 0; i < data.size(); i += batchSize) {
            List<Map<String, Object>> batch = data.subList(i, Math.min(i + batchSize, data.size()));
            insertBatch(jdbcTemplate, batch, task, mappings);
            log.info("写入批次 {}/{}, 写入 {} 条数据", (i / batchSize) + 1,
                    (data.size() + batchSize - 1) / batchSize, batch.size());
        }
    }

    private void insertBatch(JdbcTemplate jdbcTemplate, List<Map<String, Object>> batch,
                             EtlTaskConfig task, List<EtlColumnMapping> mappings) {
        if (batch.isEmpty()) {
            return;
        }

        List<String> targetColumns = getTargetColumns(mappings, batch.get(0));
        String sql = buildInsertSql(task.getTargetTable(), targetColumns);

        List<Object[]> batchArgs = new ArrayList<>();
        for (Map<String, Object> row : batch) {
            Object[] args = new Object[targetColumns.size()];
            for (int i = 0; i < targetColumns.size(); i++) {
                String col = targetColumns.get(i);
                String sourceCol = getSourceColumn(col, mappings);
                args[i] = getValue(row, sourceCol, col, mappings);
            }
            batchArgs.add(args);
        }

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    private String buildInsertSql(String tableName, List<String> columns) {
        StringBuilder sb = new StringBuilder("INSERT INTO ");
        sb.append(tableName).append(" (");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(columns.get(i));
        }
        sb.append(") VALUES (");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("?");
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
            // 没有映射配置时，使用源数据的key作为目标列
            columns.addAll(row.keySet());
        }
        return columns;
    }

    private String getSourceColumn(String targetCol, List<EtlColumnMapping> mappings) {
        if (mappings != null) {
            for (EtlColumnMapping m : mappings) {
                if (m.getTargetColumn().equalsIgnoreCase(targetCol)) {
                    return m.getSourceColumn();
                }
            }
        }
        return targetCol;
    }

    private Object getValue(Map<String, Object> row, String sourceCol, String targetCol,
                           List<EtlColumnMapping> mappings) {
        Object value = row.get(sourceCol);
        if (value == null && mappings != null) {
            for (EtlColumnMapping m : mappings) {
                if (m.getTargetColumn().equalsIgnoreCase(targetCol)) {
                    if (value == null && m.getDefaultValue() != null) {
                        return m.getDefaultValue();
                    }
                    break;
                }
            }
        }
        return value;
    }
}
