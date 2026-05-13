package com.mergedata.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *  Druid 配置类
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class DruidConfig {

    /**
     * 配置 Druid 数据源
     */
    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSource druidDataSource() {
        DruidDataSource dataSource = new DruidDataSource();

        // 额外的配置（可选）
        try {
            // 配置监控统计拦截的filters
            dataSource.setFilters("stat,wall,log4j2");
        } catch (SQLException e) {
            throw new RuntimeException("Druid configuration initialization filter error", e);
        }

        return dataSource;
    }

    /**
     * 配置 Druid 监控界面
     */
    @Bean
    public ServletRegistrationBean<StatViewServlet> statViewServlet() {
        ServletRegistrationBean<StatViewServlet> registrationBean =
                new ServletRegistrationBean<>(new StatViewServlet(), "/druid/*");

        Map<String, String> initParams = new HashMap<>();
        // ⚠️ VULN: 硬编码弱密码 admin/admin123，且无IP白名单限制(=允许所有IP)
        // 建议：使用更强的密码并配置IP白名单，生产环境建议关闭Druid监控
        initParams.put("loginUsername", "admin");
        initParams.put("loginPassword", "admin123");
        // ⚠️ VULN: IP白名单为空，允许所有来源访问Druid监控页面
        // 建议：配置具体IP或网段，如 initParams.put("allow", "127.0.0.1,192.168.")
        initParams.put("allow", "");
        // IP黑名单（存在共同时，deny优先于allow）
        // initParams.put("deny", "192.168.1.100");
        // 禁用HTML页面上的"Reset All"功能
        initParams.put("resetEnable", "false");

        registrationBean.setInitParameters(initParams);
        return registrationBean;
    }

    /**
     * 配置 Web 监控过滤器
     */
    @Bean
    public FilterRegistrationBean<WebStatFilter> webStatFilter() {
        FilterRegistrationBean<WebStatFilter> registrationBean =
                new FilterRegistrationBean<>(new WebStatFilter());

        // 添加过滤规则
        registrationBean.setUrlPatterns(Arrays.asList("/*"));

        Map<String, String> initParams = new HashMap<>();
        // 忽略过滤格式
        initParams.put("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*,/reportapi/*");
        // 开启session统计功能
        initParams.put("sessionStatEnable", "true");
        // 配置profileEnable能够监控单个url调用的sql列表
        initParams.put("profileEnable", "true");

        registrationBean.setInitParameters(initParams);
        return registrationBean;
    }

    /**
     * 配置 JdbcTemplate
     */
    @Bean
    @ConditionalOnMissingBean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}