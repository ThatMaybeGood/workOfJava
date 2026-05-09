package com.demo.integration.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2026/5/9 22:55
 */
@Data
@Component
@ConfigurationProperties(prefix = "integration")
public class IntegrationProperties {

    private TraceConfig trace;

    private Map<String, ApiConfig> apis = new HashMap<>();

    private Map<String, ReceiveConfig> receives = new HashMap<>();

    @Data
    public static class TraceConfig {

        private Boolean enabled;

        private String defaultPrefix;

        private Integer idLength;

        private Map<String, String> bizPrefix = new HashMap<>();
    }

    @Data
    public static class ApiConfig {

        private Boolean enabled;

        private String url;

        private Integer timeout;

        private Boolean printLog;

        private Boolean printResponse;

        private Boolean printError;

        private Boolean retryEnabled;

        private Integer retryCount;

        private Integer retryInterval;

        private Boolean saveMessage;

        private String contentType;
    }

    @Data
    public static class ReceiveConfig {

        private Boolean enabled;

        private Boolean processEnabled;

        private Boolean printRequest;

        private Boolean printResponse;

        private Boolean saveMessage;

        private Boolean asyncEnabled;

        private List<String> ipWhiteList;
    }
}
