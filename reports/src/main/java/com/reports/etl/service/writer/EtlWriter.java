package com.reports.etl.service.writer;

import com.reports.etl.entity.EtlDatasource;
import com.reports.etl.entity.EtlTask;
import com.reports.etl.service.core.EtlMetaDao;
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

    private final EtlMetaDao metaDao;
    private final EtlDataSourceRegistry registry;

    public EtlWriter(EtlMetaDao metaDao, EtlDataSourceRegistry registry) {
        this.metaDao = metaDao;
        this.registry = registry;
    }

    @PostConstruct
    public void init() {
        log.info("EtlWriter 初始化完成");
    }

    public void truncateTable(Long targetDsId, String table) {
        com.zaxxer.hikari.HikariDataSource pool = registry.getPool(targetDsId);
        try (Connection conn = pool.getConnection()) {
            conn.createStatement().execute("TRUNCATE TABLE " + table);
            log.info("[TRUNCATE] {} 清空成功", table);
        } catch (SQLException e) {
            log.warn("[TRUNCATE] {} 清空失败（忽略）: {}", table, e.getMessage());
        }
    }

    public int write(Long taskId, List<Map<String, Object>> rows, boolean dryRun) {
        EtlTask task = metaDao.getTask(taskId);
        if (task == null) throw new RuntimeException("任务不存在: " + taskId);

        Long targetDsId = task.getTargetDsId();
        if (targetDsId == null) throw new RuntimeException("任务未配置目标数据源");

        EtlDatasource targetDs = metaDao.getDatasource(targetDsId);
        if (targetDs == null) throw new RuntimeException("目标数据源不存在: " + targetDsId);
        String dialect = DialectResolver.resolve(targetDs.getDbType(), targetDs.getDriverClass(), targetDs.getUrl());

        com.zaxxer.hikari.HikariDataSource pool = registry.getPool(targetDsId);
        String targetTable = task.getTargetTable();
        String writeMode = task.getWriteMode() != null ? task.getWriteMode() : "INSERT";

        String[] allCols = resolveColumns(rows);
        String[] queryIndexCols = task.getQueryIndexCols() != null
                ? task.getQueryIndexCols().split(",")
                : new String[0];
        String[] updateCols = task.getUpdateCols() != null
                ? task.getUpdateCols().split(",")
                : new String[0];

        int totalWritten = 0;
        try (Connection conn = pool.getConnection()) {
            conn.setAutoCommit(false);
            boolean ok = false;
            try {
                if ("INSERT".equals(writeMode)) {
                    totalWritten = writeInsert(conn, targetTable, allCols, rows);
                } else if ("UPDATE".equals(writeMode)) {
                    totalWritten = writeUpdate(conn, targetTable, queryIndexCols, updateCols, rows);
                } else if ("UPSERT".equals(writeMode)) {
                    totalWritten = writeUpsert(conn, dialect, targetTable, allCols, queryIndexCols, updateCols, rows);
                } else {
                    totalWritten = writeInsert(conn, targetTable, allCols, rows);
                }
                if (!dryRun) {
                    conn.commit();
                } else {
                    conn.rollback();
                    log.info("[DRY-RUN] 写入 {} 行（已回滚）", totalWritten);
                }
                ok = true;
            } finally {
                if (!ok && !dryRun) {
                    try { conn.rollback(); } catch (SQLException ignored) {}
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("写入失败: " + e.getMessage(), e);
        }
        return totalWritten;
    }

    private int writeInsert(Connection conn, String table, String[] cols, List<Map<String, Object>> rows) throws SQLException {
        String placeholders = buildPlaceholders(cols.length);
        String sql = "INSERT INTO " + table + " (" + String.join(", ", cols) + ") VALUES (" + placeholders + ")";
        int total = 0;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Map<String, Object> row : rows) {
                for (int i = 0; i < cols.length; i++) {
                    stmt.setObject(i + 1, row.get(cols[i]));
                }
                stmt.addBatch();
                total++;
            }
            stmt.executeBatch();
        }
        return total;
    }

    private int writeUpdate(Connection conn, String table, String[] indexCols, String[] updateCols,
                            List<Map<String, Object>> rows) throws SQLException {
        String where = buildWhereClause(indexCols, table);
        String set = buildSetClause(updateCols);
        String sql = "UPDATE " + table + " SET " + set + " WHERE " + where;
        int total = 0;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
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
        }
        return total;
    }

    private int writeUpsert(Connection conn, String dialect, String table, String[] allCols,
                            String[] indexCols, String[] updateCols, List<Map<String, Object>> rows) throws SQLException {
        String sql = DialectResolver.getUpsertSql(table, indexCols, updateCols, allCols, dialect);
        boolean twice = DialectResolver.upsertBindsTwice(dialect);
        int total = 0;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Map<String, Object> row : rows) {
                int idx = 1;
                for (int i = 0; i < allCols.length; i++) {
                    stmt.setObject(idx++, row.get(allCols[i]));
                }
                if (twice) {
                    for (int i = 0; i < allCols.length; i++) {
                        stmt.setObject(idx++, row.get(allCols[i]));
                    }
                }
                stmt.addBatch();
                total++;
            }
            stmt.executeBatch();
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