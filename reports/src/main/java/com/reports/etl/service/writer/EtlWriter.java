package com.reports.etl.service.writer;

import com.reports.etl.entity.EtlDatasource;
import com.reports.etl.entity.EtlTask;
import com.reports.etl.mapper.EtlDatasourceMapper;
import com.reports.etl.mapper.EtlTaskMapper;
import com.reports.etl.service.registry.EtlDataSourceRegistry;
import com.reports.etl.util.DialectResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@Slf4j
@Component
public class EtlWriter {

    private final EtlTaskMapper taskMapper;
    private final EtlDatasourceMapper datasourceMapper;
    private final EtlDataSourceRegistry registry;

    public EtlWriter(EtlTaskMapper taskMapper, EtlDatasourceMapper datasourceMapper,
                     EtlDataSourceRegistry registry) {
        this.taskMapper = taskMapper;
        this.datasourceMapper = datasourceMapper;
        this.registry = registry;
    }

    @PostConstruct
    public void init() {
        log.info("EtlWriter 初始化完成");
    }

    public int write(Long taskId, List<Map<String, Object>> rows, boolean dryRun) {
        EtlTask task = taskMapper.selectById(taskId);
        if (task == null) throw new RuntimeException("任务不存在: " + taskId);

        Long targetDsId = task.getTargetDsId();
        if (targetDsId == null) throw new RuntimeException("任务未配置目标数据源");

        EtlDatasource targetDs = datasourceMapper.selectById(targetDsId);
        String dialect = DialectResolver.resolve(targetDs.getDbType(), targetDs.getDriverClass(), targetDs.getUrl());

        com.zaxxer.hikari.HikariDataSource pool = registry.getPool(targetDsId);
        String targetTable = task.getTargetTable();

        String[] allCols = resolveColumns(rows);
        String[] queryIndexCols = task.getQueryIndexCols() != null
                ? task.getQueryIndexCols().split(",")
                : new String[0];
        String[] updateCols = task.getUpdateCols() != null
                ? task.getUpdateCols().split(",")
                : new String[0];

        int totalWritten = 0;
        try {
            pool.getConnection().setAutoCommit(false);
            try {
                if ("INSERT".equals(task.getWriteMode())) {
                    totalWritten = writeInsert(pool, targetTable, allCols, rows);
                } else if ("UPDATE".equals(task.getWriteMode())) {
                    totalWritten = writeUpdate(pool, targetTable, queryIndexCols, updateCols, rows);
                } else if ("UPSERT".equals(task.getWriteMode())) {
                    totalWritten = writeUpsert(pool, targetTable, allCols, queryIndexCols, updateCols, rows);
                }
                if (!dryRun) {
                    pool.getConnection().commit();
                } else {
                    pool.getConnection().rollback();
                    log.info("[DRY-RUN] 写入 {} 行（已回滚）", totalWritten);
                }
            } catch (Exception e) {
                if (!dryRun) {
                    pool.getConnection().rollback();
                }
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("写入失败: " + e.getMessage(), e);
        }
        return totalWritten;
    }

    private int writeInsert(DataSource pool, String table, String[] cols, List<Map<String, Object>> rows) throws SQLException {
        String placeholders = buildPlaceholders(cols.length);
        String sql = "INSERT INTO " + table + " (" + String.join(", ", cols) + ") VALUES (" + placeholders + ")";
        int total = 0;
        try (Connection conn = pool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (Map<String, Object> row : rows) {
                for (int i = 0; i < cols.length; i++) {
                    stmt.setObject(i + 1, row.get(cols[i]));
                }
                stmt.addBatch();
                total++;
            }
            stmt.executeBatch();
            conn.commit();
        }
        return total;
    }

    private int writeUpdate(DataSource pool, String table, String[] indexCols, String[] updateCols,
                            List<Map<String, Object>> rows) throws SQLException {
        String where = buildWhereClause(indexCols, table);
        String set = buildSetClause(updateCols);
        String sql = "UPDATE " + table + " SET " + set + " WHERE " + where;
        int total = 0;
        try (Connection conn = pool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (Map<String, Object> row : rows) {
                int paramIdx = 1;
                for (String col : updateCols) {
                    stmt.setObject(paramIdx++, row.get(col));
                }
                for (String col : indexCols) {
                    stmt.setObject(paramIdx++, row.get(col));
                }
                stmt.addBatch();
                total++;
            }
            stmt.executeBatch();
            conn.commit();
        }
        return total;
    }

    private int writeUpsert(DataSource pool, String table, String[] allCols, String[] indexCols,
                            String[] updateCols, List<Map<String, Object>> rows) throws SQLException {
        String sql = DialectResolver.getUpsertSql(table, indexCols, updateCols, allCols,
                DialectResolver.resolve(null, null, null));
        int total = 0;
        try (Connection conn = pool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (Map<String, Object> row : rows) {
                for (int i = 0; i < allCols.length; i++) {
                    stmt.setObject(i + 1, row.get(allCols[i]));
                }
                stmt.addBatch();
                total++;
            }
            stmt.executeBatch();
            conn.commit();
        }
        return total;
    }

    private String[] resolveColumns(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) return new String[0];
        return rows.get(0).keySet().toArray(new String[0]);
    }

    private String buildPlaceholders(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append(", ");
            sb.append("?");
        }
        return sb.toString();
    }

    private String buildWhereClause(String[] indexCols, String table) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indexCols.length; i++) {
            if (i > 0) sb.append(" AND ");
            sb.append(table).append(".").append(indexCols[i]).append(" = ?");
        }
        return sb.toString();
    }

    private String buildSetClause(String[] updateCols) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < updateCols.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(updateCols[i]).append(" = ?");
        }
        return sb.toString();
    }
}