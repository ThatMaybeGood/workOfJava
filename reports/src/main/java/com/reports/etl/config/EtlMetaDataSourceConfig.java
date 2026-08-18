package com.reports.etl.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

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
        HikariDataSource dataSource = new HikariDataSource(config);
        initSchema(dataSource);
        return dataSource;
    }

    @Bean(name = "etlMetaJdbcTemplate")
    public JdbcTemplate etlMetaJdbcTemplate(@Qualifier("etlMetaDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * 数据源创建后立即执行建表脚本，保证任何使用方拿到连接时表已存在
     */
    private void initSchema(DataSource dataSource) {
        try {
            java.io.InputStream is = new ClassPathResource("db/etl_meta_init.sql").getInputStream();
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int len;
            while ((len = is.read(buf)) != -1) {
                bos.write(buf, 0, len);
            }
            is.close();
            String sql = new String(bos.toByteArray(), StandardCharsets.UTF_8);
            try (java.sql.Connection conn = dataSource.getConnection();
                 java.sql.Statement stmt = conn.createStatement()) {
                for (String s : sql.split(";")) {
                    String trimmed = s.trim();
                    // 去掉行首注释行
                    StringBuilder sb = new StringBuilder();
                    for (String line : trimmed.split("\\r?\\n")) {
                        String t = line.trim();
                        if (!t.startsWith("--") && !t.isEmpty()) {
                            sb.append(line).append('\n');
                        }
                    }
                    String stmtSql = sb.toString().trim();
                    if (stmtSql.isEmpty()) continue;
                    stmt.execute(stmtSql);
                }
            }
            log.info("ETL 元数据库建表脚本执行完成");
        } catch (Exception e) {
            log.error("ETL 元数据库建表脚本执行失败: {}", e.getMessage(), e);
            throw new IllegalStateException("ETL 元数据库初始化失败", e);
        }
    }
}