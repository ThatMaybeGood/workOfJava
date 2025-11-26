package com.example.messagedataservice.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "external-api")
public class ApiProperties {

    // 读取所有的 base-urls 映射
    private Map<String, String> baseUrls;

    // 读取 endpoint-path
    private String endpointPath;

    // Getters and Setters (Spring Boot 需要这些来注入值)

    public Map<String, String> getBaseUrls() {
        return baseUrls;
    }

    public void setBaseUrls(Map<String, String> baseUrls) {
        this.baseUrls = baseUrls;
    }

    public String getEndpointPath() {
        return endpointPath;
    }

    public void setEndpointPath(String endpointPath) {
        this.endpointPath = endpointPath;
    }
}