package com.reports.config;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 动态数据源配置（支持任意多个数据源，配置即注册）
 * <p>
 * yml 中 {@code spring.datasource} 下每新增一个子配置，就自动注册为一个数据源，
 * 路由 key 即为该子配置名（master / slave / b / c ...），加库无需改代码。示例：
 * <pre>
 *   spring:
 *     datasource:
 *       master: { url, username, password, ... }   # 主库，作为默认数据源
 *       slave:  { url, username, password, ... }   # 新增库：加一段即可
 * </pre>
 * mapper 方法未加 {@link com.reports.annotation.DataSource} 注解时，默认走
 * {@code master}（key 由 {@link DynamicDataSourceContextHolder#DEFAULT_DS} 约定）。
 */
@Slf4j
@Configuration
public class DataSourceConfig {

    /**
     * 将 yml {@code spring.datasource} 下所有子配置，各绑定为一个 DruidDataSource。
     *
     * @return key = 数据源名（master/slave/...），value = 对应连接池；空 map 由 Spring 绑定填充
     */
    @Bean("namedDataSources")
    @ConfigurationProperties(prefix = "spring.datasource")
    public Map<String, DruidDataSource> namedDataSources() {
        return new LinkedHashMap<>();
    }

    /**
     * 动态数据源路由：注册全部数据源到路由表，以 master 为默认。
     */
    @Bean("dynamicDataSource")
    @Primary
    public DataSource dynamicDataSource(
            @Qualifier("namedDataSources") Map<String, DruidDataSource> namedDataSources) {
        Map<Object, Object> targetDataSources = new LinkedHashMap<>();
        for (Map.Entry<String, DruidDataSource> entry : namedDataSources.entrySet()) {
            targetDataSources.put(entry.getKey(), entry.getValue());
            log.info("注册数据源: key=[{}]", entry.getKey());
        }

        DruidDataSource defaultDataSource = namedDataSources.get(DynamicDataSourceContextHolder.DEFAULT_DS);
        if (defaultDataSource == null) {
            throw new IllegalStateException(
                    "缺少默认数据源，请在 yml 配置 spring.datasource."
                            + DynamicDataSourceContextHolder.DEFAULT_DS);
        }

        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.setTargetDataSources(targetDataSources);
        dynamicDataSource.setDefaultTargetDataSource(defaultDataSource);
        log.info("动态数据源初始化完成，共 {} 个，默认数据源: [{}]",
                targetDataSources.size(), DynamicDataSourceContextHolder.DEFAULT_DS);
        return dynamicDataSource;
    }
}
