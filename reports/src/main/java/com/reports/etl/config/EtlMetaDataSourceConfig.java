package com.reports.etl.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;

/**
 * ETL 元数据库配置（JDBC 方式，避免与 MyBatis-Plus 多数据源冲突）
 * 独立 H2 文件库，与 Oracle 报表库完全隔离
 */
@Slf4j
@Configuration
public class EtlMetaDataSourceConfig {

    @Bean(name = "etlMetaDataSource")
    public DataSource etlMetaDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:file:./data/etl_meta;DB_CLOSE_ON_EXIT=FALSE");
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setPoolName("etl-meta-h2");
        config.setConnectionInitSql("SELECT 1");
        return new HikariDataSource(config);
    }

    @Bean(name = "etlMetaJdbcTemplate")
    public JdbcTemplate etlMetaJdbcTemplate(@Qualifier("etlMetaDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * 启动时执行建表脚本
     */
    @Bean
    public Object etlMetaSchemaInit(@Qualifier("etlMetaDataSource") DataSource dataSource) {
        try {
            java.io.InputStream is = new ClassPathResource("db/etl_meta_init.sql").getInputStream();
            byte[] bytes = is.readAllBytes();
            String sql = new String(bytes, StandardCharsets.UTF_8);
            try (java.sql.Connection conn = dataSource.getConnection();
                 java.sql.Statement stmt = conn.createStatement()) {
                for (String s : sql.split(";")) {
                    if (s.trim().startsWith("--") || s.trim().isEmpty()) continue;
                    stmt.execute(s.trim().endsWith(";") ? s.trim() : s.trim() + ";");
                }
            }
            log.info("ETL 元数据库建表脚本执行完成");
        } catch (Exception e) {
            log.warn("ETL 元数据库建表脚本执行跳过（可能已存在）: {}", e.getMessage());
        }
        return new Object();
    }
}