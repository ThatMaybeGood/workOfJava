package com.reports.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 分页默认配置
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "reports.page")
public class PageConfig {

    /**
     * 默认当前页码
     */
    private Integer defaultPage = 1;

    /**
     * 默认每页条数
     */
    private Integer defaultPageSize = 10;

}
