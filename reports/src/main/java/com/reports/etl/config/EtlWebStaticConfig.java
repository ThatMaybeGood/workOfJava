package com.reports.etl.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 把项目根下 reports-web/etl/ 以 /etl/** 暴露为静态资源，
 * 使 ETL 前端与 /api/etl 同源（相对路径请求，免 CORS）。
 *
 * 注意：工作目录通常为 workOfJava/（IDEA 启动），而非 reports/，
 * 故使用 System.getProperty("user.dir") 动态定位绝对路径。
 */
@Configuration
public class EtlWebStaticConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 动态定位 reports-web/，兼容跨平台与多种启动方式：
        //   - IDEA 启动：cwd = workOfJava/  → 需要 reports/reports-web
        //   - Maven 启动：cwd = reports/     → 直接 reports-web
        //   - Windows 路径分隔符为 \，统一转 / 再判断
        String userDir = System.getProperty("user.dir").replace('\\', '/');
        boolean isReportsDir = userDir.endsWith("/reports");
        String basePath = isReportsDir ? "reports-web" : "reports/reports-web";

        registry.addResourceHandler("/etl/**")
                .addResourceLocations("file:" + basePath + "/etl/");
        // 前端公共依赖（bootstrap/icons 等），etl/index.html 以 ../vendor 相对路径引用
        registry.addResourceHandler("/vendor/**")
                .addResourceLocations("file:" + basePath + "/vendor/");
        // 主入口 index.html（含侧边栏导航）及全局样式/脚本，供 http://host/ 直接访问
        registry.addResourceHandler("/**")
                .addResourceLocations("file:" + basePath + "/");
    }
}
