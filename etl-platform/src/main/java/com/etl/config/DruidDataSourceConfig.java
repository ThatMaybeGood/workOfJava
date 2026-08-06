package com.etl.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DruidDataSourceConfig {

    @Value("${spring.datasource.druid.stat-view-servlet.login-username:admin}")
    private String druidUser;

    @Value("${spring.datasource.druid.stat-view-servlet.login-password:admin123}")
    private String druidPass;

    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSource dataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        try {
            dataSource.setFilters("stat,wall,log4j2");
        } catch (SQLException e) {
            throw new RuntimeException("Druid filter configuration error", e);
        }
        return dataSource;
    }

    @Bean
    public ServletRegistrationBean<StatViewServlet> statViewServlet() {
        ServletRegistrationBean<StatViewServlet> registration =
                new ServletRegistrationBean<>(new StatViewServlet(), "/druid/*");
        Map<String, String> initParams = new HashMap<>();
        initParams.put("loginUsername", druidUser);
        initParams.put("loginPassword", druidPass);
        initParams.put("allow", "");
        initParams.put("resetEnable", "false");
        registration.setInitParameters(initParams);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<WebStatFilter> webStatFilter() {
        FilterRegistrationBean<WebStatFilter> registration =
                new FilterRegistrationBean<>(new WebStatFilter());
        registration.setUrlPatterns(Arrays.asList("/*"));
        Map<String, String> initParams = new HashMap<>();
        initParams.put("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
        initParams.put("sessionStatEnable", "true");
        initParams.put("profileEnable", "true");
        registration.setInitParameters(initParams);
        return registration;
    }
}
