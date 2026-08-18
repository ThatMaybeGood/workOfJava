package com.reports.etl.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 把项目根下 reports-web/etl/ 以 /etl/** 暴露为静态资源，
 * 使 ETL 前端与 /api/etl 同源（相对路径请求，免 CORS）。
 */
@Configuration
public class EtlWebStaticConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/etl/**")
                .addResourceLocations("file:reports-web/etl/");
        // 前端公共依赖（bootstrap/icons 等），etl/index.html 以 ../vendor 相对路径引用
        registry.addResourceHandler("/vendor/**")
                .addResourceLocations("file:reports-web/vendor/");
    }
}
