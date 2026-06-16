package com.reports.config;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 多数据源配置
 * <p>
 * 支持灵活配置：
 * 1. 只配置 master 时，slave 数据源不会创建，程序正常启动
 * 2. 同时配置 master + slave 时，支持动态切换
 * 3. 通过 {@link com.reports.annotation.DataSource} 注解声明式切换数据源
 */
@Slf4j
@Configuration
public class DataSourceConfig {

    /**
     * 主数据源（必须配置）
     */
    @Bean("masterDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.master")
    public DataSource masterDataSource() {
        return new DruidDataSource();
    }

    /**
     * 从数据源（可选配置）
     * 当 spring.datasource.slave.url 存在时才创建此 Bean
     */
    @Bean("slaveDataSource")
    @ConditionalOnProperty(prefix = "spring.datasource.slave", name = "url")
    @ConfigurationProperties(prefix = "spring.datasource.slave")
    public DataSource slaveDataSource() {
        return new DruidDataSource();
    }

    /**
     * 动态数据源
     */
    @Bean("dynamicDataSource")
    @Primary
    public DataSource dynamicDataSource(
            @Qualifier("masterDataSource") DataSource masterDataSource,
            @Qualifier("slaveDataSource") DataSource slaveDataSource) {
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("master", masterDataSource);

        // 如果 slave 数据源存在，加入路由表
        if (slaveDataSource != null) {
            targetDataSources.put("slave", slaveDataSource);
            log.info("slave 数据源已配置，支持动态切换");
        } else {
            log.info("slave 数据源未配置，仅使用 master 数据源");
        }

        dynamicDataSource.setTargetDataSources(targetDataSources);
        dynamicDataSource.setDefaultTargetDataSource(masterDataSource);
        return dynamicDataSource;
    }

}
