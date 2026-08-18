package com.reports.etl.service.registry;

import com.reports.etl.entity.EtlDatasource;
import com.reports.etl.service.core.EtlMetaDao;
import com.reports.etl.util.DialectResolver;
import com.reports.etl.util.DsEncryptUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class EtlDataSourceRegistry {

    private final EtlMetaDao metaDao;
    private final ConcurrentHashMap<Long, HikariDataSource> poolCache = new ConcurrentHashMap<>();

    public EtlDataSourceRegistry(EtlMetaDao metaDao) {
        this.metaDao = metaDao;
    }

    public HikariDataSource getPool(Long dsId) {
        return poolCache.computeIfAbsent(dsId, id -> {
            EtlDatasource ds = metaDao.getDatasource(id);
            if (ds == null) throw new RuntimeException("数据源不存在: " + id);
            if (ds.getEnabled() == null || ds.getEnabled() != 1) throw new RuntimeException("数据源已禁用: " + ds.getName());
            return buildPool(ds);
        });
    }

    public void refresh(Long dsId) {
        poolCache.remove(dsId);
        EtlDatasource ds = metaDao.getDatasource(dsId);
        if (ds != null) {
            poolCache.put(dsId, buildPool(ds));
        }
    }

    public void remove(Long dsId) {
        HikariDataSource pool = poolCache.remove(dsId);
        if (pool != null) pool.close();
    }

    public void evictAll() {
        poolCache.values().forEach(HikariDataSource::close);
        poolCache.clear();
    }

    private HikariDataSource buildPool(EtlDatasource ds) {
        HikariConfig config = new HikariConfig();
        String dbType = DialectResolver.resolve(ds.getDbType(), ds.getDriverClass(), ds.getUrl());
        String password = DsEncryptUtil.decrypt(ds.getPassword());

        config.setJdbcUrl(ds.getUrl());
        config.setUsername(ds.getUsername());
        config.setPassword(password);
        config.setDriverClassName(resolveDriver(dbType));
        config.setPoolName("etl-" + ds.getId() + "-" + ds.getName());
        config.setMaximumPoolSize(ds.getPoolMaxActive() != null ? ds.getPoolMaxActive() : 10);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000L);
        config.setIdleTimeout(600000L);
        config.setMaxLifetime(1800000L);
        return new HikariDataSource(config);
    }

    public String resolveDriver(String dbType) {
        switch (dbType) {
            case "MYSQL": return "com.mysql.cj.jdbc.Driver";
            case "POSTGRESQL": return "org.postgresql.Driver";
            case "SQLSERVER": return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            case "DM": return "dm.jdbc.driver.DmDriver";
            case "H2": return "org.h2.Driver";
            case "ORACLE": default: return "oracle.jdbc.OracleDriver";
        }
    }

    public String resolveDialect(EtlDatasource ds) {
        return DialectResolver.resolve(ds.getDbType(), ds.getDriverClass(), ds.getUrl());
    }

    public Map<String, Object> testConnection(Long dsId) {
        HikariDataSource pool = getPool(dsId);
        try (Connection conn = pool.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("driverName", meta.getDriverName());
            result.put("databaseProductName", meta.getDatabaseProductName());
            result.put("url", meta.getURL());
            result.put("user", meta.getUserName());
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    public List<Map<String, String>> getTables(Long dsId) {
        HikariDataSource pool = getPool(dsId);
        List<Map<String, String>> result = new ArrayList<>();
        try (Connection conn = pool.getConnection();
             ResultSet rs = conn.getMetaData().getTables(null, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                Map<String, String> row = new HashMap<>();
                row.put("tableName", rs.getString("TABLE_NAME"));
                row.put("remarks", rs.getString("REMARKS"));
                result.add(row);
            }
        } catch (Exception e) {
            log.error("获取表列表失败 dsId={}", dsId, e);
            throw new RuntimeException("获取表列表失败: " + e.getMessage(), e);
        }
        return result;
    }

    public List<Map<String, Object>> getColumns(Long dsId, String tableName) {
        HikariDataSource pool = getPool(dsId);
        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection conn = pool.getConnection();
             ResultSet rs = conn.getMetaData().getColumns(null, null, tableName, "%")) {
            while (rs.next()) {
                Map<String, Object> col = new LinkedHashMap<>();
                col.put("columnName", rs.getString("COLUMN_NAME"));
                col.put("dataType", rs.getString("TYPE_NAME"));
                col.put("nullable", rs.getBoolean("NULLABLE"));
                col.put("comment", rs.getString("REMARKS"));
                result.add(col);
            }
        } catch (Exception e) {
            log.error("获取表列失败 dsId={} table={}", dsId, tableName, e);
            throw new RuntimeException("获取表列失败: " + e.getMessage(), e);
        }
        return result;
    }
}