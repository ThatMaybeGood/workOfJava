package com.etl.service.core;

import com.alibaba.druid.pool.DruidDataSource;
import com.etl.entity.DatasourceConfig;
import com.etl.service.admin.DatasourceConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class DataSourceManager {

    private final Map<String, DruidDataSource> dataSourceMap = new ConcurrentHashMap<>();
    private final Map<String, JdbcTemplate> jdbcTemplateMap = new ConcurrentHashMap<>();

    @Autowired
    @Lazy
    private DatasourceConfigService datasourceConfigService;

    public JdbcTemplate getJdbcTemplate(String dsName) {
        JdbcTemplate jdbcTemplate = jdbcTemplateMap.get(dsName);
        if (jdbcTemplate != null) {
            return jdbcTemplate;
        }

        synchronized (this) {
            jdbcTemplate = jdbcTemplateMap.get(dsName);
            if (jdbcTemplate != null) {
                return jdbcTemplate;
            }

            DatasourceConfig config = datasourceConfigService.getByName(dsName);
            if (config == null) {
                throw new RuntimeException("数据源不存在: " + dsName);
            }

            DruidDataSource dataSource = createDataSource(config);
            dataSourceMap.put(dsName, dataSource);
            jdbcTemplate = new JdbcTemplate(dataSource);
            jdbcTemplateMap.put(dsName, jdbcTemplate);
            return jdbcTemplate;
        }
    }

    public DataSource getDataSource(String dsName) {
        DruidDataSource ds = dataSourceMap.get(dsName);
        if (ds != null) {
            return ds;
        }

        synchronized (this) {
            ds = dataSourceMap.get(dsName);
            if (ds != null) {
                return ds;
            }

            DatasourceConfig config = datasourceConfigService.getByName(dsName);
            if (config == null) {
                throw new RuntimeException("数据源不存在: " + dsName);
            }

            ds = createDataSource(config);
            dataSourceMap.put(dsName, ds);
            return ds;
        }
    }

    private DruidDataSource createDataSource(DatasourceConfig config) {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(config.getDriverClass());
        dataSource.setUrl(config.getJdbcUrl());
        dataSource.setUsername(config.getUsername());
        dataSource.setPassword(config.getPassword());

        dataSource.setInitialSize(config.getInitialSize() != null ? config.getInitialSize() : 5);
        dataSource.setMinIdle(config.getMinIdle() != null ? config.getMinIdle() : 5);
        dataSource.setMaxActive(config.getMaxActive() != null ? config.getMaxActive() : 20);
        dataSource.setMaxWait(config.getMaxWait() != null ? config.getMaxWait() : 60000);
        dataSource.setValidationQuery(config.getValidationQuery() != null ? config.getValidationQuery() : "SELECT 1 FROM DUAL");
        dataSource.setTestOnBorrow("Y".equals(config.getTestOnBorrow()));
        dataSource.setTestWhileIdle("Y".equals(config.getTestWhileIdle()));

        try {
            dataSource.init();
            log.info("数据源 [{}] 初始化成功", config.getDsName());
        } catch (SQLException e) {
            log.error("数据源 [{}] 初始化失败", config.getDsName(), e);
            throw new RuntimeException("数据源初始化失败: " + config.getDsName(), e);
        }

        return dataSource;
    }

    public boolean testConnection(DatasourceConfig config) {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(config.getDriverClass());
        dataSource.setUrl(config.getJdbcUrl());
        dataSource.setUsername(config.getUsername());
        dataSource.setPassword(config.getPassword());
        dataSource.setValidationQuery(config.getValidationQuery() != null ? config.getValidationQuery() : "SELECT 1 FROM DUAL");
        dataSource.setConnectionErrorRetryAttempts(1);
        dataSource.setBreakAfterAcquireFailure(true);

        try {
            dataSource.init();
            boolean valid = dataSource.getConnection().isValid(5);
            dataSource.close();
            return valid;
        } catch (Exception e) {
            log.warn("数据源连接测试失败: {}", config.getDsName(), e);
            return false;
        }
    }

    public void removeDataSource(String dsName) {
        DruidDataSource ds = dataSourceMap.remove(dsName);
        jdbcTemplateMap.remove(dsName);
        if (ds != null) {
            ds.close();
            log.info("数据源 [{}] 已移除", dsName);
        }
    }

    public void refreshDataSource(String dsName) {
        removeDataSource(dsName);
        getJdbcTemplate(dsName);
    }
}
