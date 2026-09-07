package com.reports.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * ETL 按需补数触发配置
 * <p>
 * 对应配置文件前缀 {@code reports.etl}，供切面 EtlTriggerAspect 等读取使用。
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "reports.etl")
public class EtlProperties {

    /**
     * 总开关：false = 整个 ETL 触发功能关闭
     */
    private boolean enabled = true;

    /**
     * ETL 平台 run 接口地址
     */
    private String runUrl;

    /**
     * 鉴权 token（建议部署时通过环境变量注入，勿硬编码在配置文件中）
     */
    private String apiToken;

    /**
     * 同一（任务+参数）两次触发的最小间隔（秒）
     */
    private long cooldownSeconds = 600L;

    /**
     * 单任务停用名单：元素为被停用 ETL 任务的 taskToken（真实值）。
     * 命中名单内任务时 ensure 直接跳过；适合“只停某一个报表的补数、其余保留”，无需改注解。
     */
    private List<String> disabledTaskTokens = new ArrayList<>();

    /**
     * HTTP 连接超时时间（秒）
     */
    private int connectTimeoutSeconds = 2;

    /**
     * HTTP 读取超时时间（秒）
     */
    private int readTimeoutSeconds = 5;

}
