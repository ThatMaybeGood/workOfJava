package com.mergedata.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
@Configuration
public class GlobalCorsFilter {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 1. 允许哪些域名访问
        // 内网用 allowedOriginPatterns("*")，比 allowedOrigins("*") 更强大
        // 且兼容 allowCredentials(true)
        config.addAllowedOriginPattern("*");

        // 2. 是否允许发送 Cookie 或 身份认证信息
        config.setAllowCredentials(true);

        // 3. 允许的请求方式 (GET, POST, OPTIONS 等)
        config.addAllowedMethod("*");

        // 4. 允许的请求头 (Token, Content-Type 等)
        config.addAllowedHeader("*");

        // 5. 预检请求（OPTIONS）的缓存时间，单位为秒
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}