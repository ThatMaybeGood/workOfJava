package com.mergedata.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
@Configuration
public class GlobalCorsFilter {
    // ⚠️ VULN: 整个项目缺少认证/鉴权机制(无Spring Security/Shiro/Token校验)
    // 所有接口(包括写入、删除操作)无需登录即可访问，任何知道接口地址的人均可操作数据
    // 建议：引入Spring Security或至少增加Token拦截器验证请求来源合法性

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // ⚠️ VULN: 允许所有来源+允许凭证=任意网站可跨域读取敏感数据
        // 建议：生产环境应配置具体域名，如 addAllowedOriginPattern("http://yourdomain.com")
        config.addAllowedOriginPattern("*");

        // ⚠️ VULN: 允许携带Cookie凭证跨域发送，配合上面"*"使用风险极高
        // 建议：仅在需要时开启，并配合具体域名
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