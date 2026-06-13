package com.reports.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 报表数据模式配置
 * 控制报表查询走 Mock 数据还是真实数据库
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "reports.data")
public class ReportDataConfig {

    /**
     * 数据模式: mock(默认) / jdbc / mybatis-plus
     */
    private String mode = "mock";

    /**
     * 是否使用 Mock 数据
     */
    public boolean isMock() {
        return "mock".equalsIgnoreCase(mode);
    }

    /**
     * 是否使用 JdbcTemplate
     */
    public boolean isJdbc() {
        return "jdbc".equalsIgnoreCase(mode);
    }

    /**
     * 是否使用 MyBatis-Plus
     */
    public boolean isMybatisPlus() {
        return "mybatis-plus".equalsIgnoreCase(mode);
    }

}
