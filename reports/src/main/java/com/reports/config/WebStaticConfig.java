package com.reports.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 把项目根下 reports-web/ 暴露为站点静态资源（主入口 index.html 及报表页面）。
 *
 * 注意：工作目录通常为 workOfJava/（IDEA 启动），而非 reports/，
 * 故使用 System.getProperty("user.dir") 动态定位绝对路径。
 */
@Configuration
public class WebStaticConfig implements WebMvcConfigurer {

    /**
     * 静态资源统一 no-cache（每次带 If-Modified-Since 回源校验，没改就是 304，几乎零开销）。
     *
     * 默认只发 Last-Modified，浏览器会按"启发式缓存"在一段时间内直接用本地副本不回源，
     * 于是改了 js/css 用户刷新还是旧的。加版本号治标不治本——HTML 本身被缓存时，
     * 里面的新版本号根本没机会被看到，所以这里从响应头一刀切掉。
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 动态定位 reports-web/，兼容跨平台与多种启动方式：
        //   - IDEA 启动：cwd = workOfJava/  → 需要 reports/reports-web
        //   - Maven 启动：cwd = reports/     → 直接 reports-web
        //   - Windows 路径分隔符为 \，统一转 / 再判断
        String userDir = System.getProperty("user.dir").replace('\\', '/');
        boolean isReportsDir = userDir.endsWith("/reports");
        String basePath = isReportsDir ? "reports-web" : "reports/reports-web";

        // 前端公共依赖（bootstrap/echarts 等），报表页面以 ../vendor 相对路径引用
        registry.addResourceHandler("/vendor/**")
                .addResourceLocations("file:" + basePath + "/vendor/")
                .setCacheControl(CacheControl.noCache());
        // 主入口 index.html（含侧边栏导航）及全局样式/脚本，供 http://host/ 直接访问
        registry.addResourceHandler("/**")
                .addResourceLocations("file:" + basePath + "/")
                .setCacheControl(CacheControl.noCache());
    }
}
