package com.reports.etl.service.registry;

import com.reports.etl.entity.EtlDatasource;
import com.reports.etl.util.DialectResolver;
import com.reports.etl.util.DsEncryptUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class EtlDatasourceJdbcService {

    private final JdbcTemplate jdbcTemplate;
    private final Map<Long, HikariDataSource> poolCache = new ConcurrentHashMap<>();

    public EtlDatasourceJdbcService(@org.springframework.beans.factory.annotation.Qualifier("etlMetaJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ========== CRUD ==========

    public List<EtlDatasource> listAll() {
        return jdbcTemplate.query("SELECT * FROM etl_datasource ORDER BY id DESC", rs -> {
            EtlDatasource ds = new EtlDatasource();
            ds.setId(rs.getLong("id"));
            ds.setName(rs.getString("name"));
            ds.setDbType(rs.getString("db_type"));
            ds.setUrl(rs.getString("url"));
            ds.setUsername(rs.getString("username"));
            ds.setPassword(rs.getString("password"));
            ds.setRole(rs.getString("role"));
            ds.setEnabled(rs.getInt("enabled"));
            return ds;
        });
    }

    public EtlDatasource getById(Long id) {
        List<EtlDatasource> list = jdbcTemplate.query("SELECT * FROM etl_datasource WHERE id=?", rs -> {
            EtlDatasource ds = new EtlDatasource();
            ds.setId(rs.getLong("id"));
            ds.setName(rs.getString("name"));
            ds.setDbType(rs.getString("db_type"));
            ds.setUrl(rs.getString("url"));
            ds.setUsername(rs.getString("username"));
            ds.setPassword(rs.getString("password"));
            ds.setRole(rs.getString("role"));
            ds.setEnabled(rs.getInt("enabled"));
            return ds;
        }, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public Long add(EtlDatasource ds) {
        jdbcTemplate.update("INSERT INTO etl_datasource(name, db_type, driver_class, url, username, password, role, pool_initial_size, pool_max_active, enabled) VALUES(?,?,?,?,?,?,?,?,?,?)",
                ds.getName(), ds.getDbType(), ds.getDriverClass(), ds.getUrl(),
                ds.getUsername(), ds.getPassword(), ds.getRole(),
                ds.getPoolInitialSize(), ds.getPoolMaxActive(), ds.getEnabled());
        return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public void update(EtlDatasource ds) {
        jdbcTemplate.update("UPDATE etl_datasource SET name=?, db_type=?, driver_class=?, url=?, username=?, password=?, role=?, pool_initial_size=?, pool_max_active=?, enabled=? WHERE id=?",
                ds.getName(), ds.getDbType(), ds.getDriverClass(), ds.getUrl(),
                ds.getUsername(), ds.getPassword(), ds.getRole(),
                ds.getPoolInitialSize(), ds.getPoolMaxActive(), ds.getEnabled(), ds.getId());
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM etl_datasource WHERE id=?", id);
        poolCache.remove(id);
    }

    // ========== 连接测试与元数据 ==========

    public Map<String, Object> testConnection(Long dsId) {
        HikariDataSource pool = getOrCreatePool(dsId);
        try (Connection conn = pool.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            return Map.of("success", true,
                    "driverName", meta.getDriverName(),
                    "databaseProductName", meta.getDatabaseProductName(),
                    "url", conn.getMetaData().getURL(),
                    "user", conn.getMetaData().getUserName());
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    public List<Map<String, String>> getTables(Long dsId) {
        HikariDataSource pool = getOrCreatePool(dsId);
        List<Map<String, String>> result = new ArrayList<>();
        try (Connection conn = pool.getConnection();
             ResultSet rs = conn.getMetaData().getTables(null, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                Map<String, String> row = new HashMap<>();
                row.put("tableName", rs.getString("TABLE_NAME"));
                result.add(row);
            }
        } catch (Exception e) {
            throw new RuntimeException("获取表列表失败: " + e.getMessage(), e);
        }
        return result;
    }

    public List<Map<String, Object>> getColumns(Long dsId, String tableName) {
        HikariDataSource pool = getOrCreatePool(dsId);
        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection conn = pool.getConnection();
             ResultSet rs = conn.getMetaData().getColumns(null, null, tableName, "%")) {
            while (rs.next()) {
                Map<String, Object> col = new LinkedHashMap<>();
                col.put("columnName", rs.getString("COLUMN_NAME"));
                col.put("dataType", rs.getString("TYPE_NAME"));
                col.put("nullable", rs.getBoolean("NULLABLE"));
                result.add(col);
            }
        } catch (Exception e) {
            throw new RuntimeException("获取表列失败: " + e.getMessage(), e);
        }
        return result;
    }

    // ========== 内部方法 ==========

    private HikariDataSource getOrCreatePool(Long dsId) {
        return poolCache.computeIfAbsent(dsId, id -> {
            EtlDatasource ds = getById(id);
            if (ds == null) throw new RuntimeException("数据源不存在: " + id);
            return buildPool(ds);
        });
    }

    private HikariDataSource buildPool(EtlDatasource ds) {
        HikariConfig config = new HikariConfig();
        String dbType = DialectResolver.resolve(ds.getDbType(), ds.getDriverClass(), ds.getUrl());
        String password = DsEncryptUtil.decrypt(ds.getPassword());
        config.setJdbcUrl(ds.getUrl());
        config.setUsername(ds.getUsername());
        config.setPassword(password);
        config.setDriverClassName(resolveDriver(dbType));
        config.setPoolName("etl-" + ds.getId());
        config.setMaximumPoolSize(ds.getPoolMaxActive() != null ? ds.getPoolMaxActive() : 10);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000L);
        return new HikariDataSource(config);
    }

    private String resolveDriver(String dbType) {
        switch (dbType) {
            case "MYSQL": return "com.mysql.cj.jdbc.Driver";
            case "POSTGRESQL": return "org.postgresql.Driver";
            case "SQLSERVER": return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            case "DM": return "dm.jdbc.driver.DmDriver";
            case "H2": return "org.h2.Driver";
            case "ORACLE": default: return "oracle.jdbc.OracleDriver";
        }
    }

    private EtlDatasource mapToDatasource(ResultSet rs) throws Exception {
        EtlDatasource ds = new EtlDatasource();
        ds.setId(rs.getLong("id"));
        ds.setName(rs.getString("name"));
        ds.setDbType(rs.getString("db_type"));
        ds.setDriverClass(rs.getString("driver_class"));
        ds.setUrl(rs.getString("url"));
        ds.setUsername(rs.getString("username"));
        ds.setPassword(rs.getString("password"));
        ds.setRole(rs.getString("role"));
        ds.setPoolInitialSize(rs.getInt("pool_initial_size"));
        ds.setPoolMaxActive(rs.getInt("pool_max_active"));
        ds.setEnabled(rs.getInt("enabled"));
        return ds;
    }

    public void evictAll() {
        poolCache.values().forEach(HikariDataSource::close);
        poolCache.clear();
    }
}