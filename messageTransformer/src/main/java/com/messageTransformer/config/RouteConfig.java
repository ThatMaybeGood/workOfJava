package com.messageTransformer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "message")
public class RouteConfig {

    private Map<String, RouteInfo> routes;

    @Data
    public static class RouteInfo {
        private String strategy;
        private String targetUrl;
    }
}