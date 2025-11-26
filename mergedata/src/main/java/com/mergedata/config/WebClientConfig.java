package com.mergedata.config;

// WebClientConfig.java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient genericWebClient(WebClient.Builder builder) {
        // 不设置 baseUrl，以便在调用时使用完整 URL
        return builder.build();
    }
}