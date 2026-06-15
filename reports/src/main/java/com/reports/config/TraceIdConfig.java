package com.reports.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 追踪号配置类
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "reports.trace")
public class TraceIdConfig {

    /**
     * 追踪号前缀
     */
    private String prefix = "YQ";

    /**
     * 追踪号数字长度
     */
    private Integer numberLength = 9;

    /**
     * 是否包含方括号
     */
    private Boolean includeBrackets = false;

}
