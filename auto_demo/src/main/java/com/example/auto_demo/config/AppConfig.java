package com.example.auto_demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class AppConfig {

    @Value("${browser.port}")
    private String browserPort;

    @Value("${yb.insutype}")
    private String insuType;

    @Value("${yb.insutype.cn}")
    private String insuTypeCn;

    @Value("${yb.bill.url}")
    private String billUrl;

    @Value("${yb.token}")
    private String token;

    @Value("${yb.session}")
    private String session;

    @Value("${yb.pageSize}")
    private String pageSize;

    @Value("${yb.frontUrl}")
    private String frontUrl;

    @Value("${yb.fixmedinsCode}")
    private String fixmedinsCode;

    // 注入是否开启调用导出接口（直接注入字符串）
    @Value("${isOpenExportInterface}")
    private boolean isOpenExportInterface;

    public AppConfig() {
        System.out.println("AppConfig 构造函数执行...");
    }

    @PostConstruct
    public void init() {
        System.out.println("配置初始化完成，insuType: " + insuType);
     }



    // Getter 方法
    public String getBrowserPort() {
        return browserPort;
    }

    public String getInsuType() {
        return insuType;
    }

    public String getInsuTypeCn() {
        return insuTypeCn;
    }

    public String getBillUrl() {
        return billUrl;
    }

    public String getToken() {
        return token;
    }

    public String getSession() {
        return session;
    }

    public String getPageSize() {
        return pageSize;
    }

    public String getFrontUrl() {
        return frontUrl;
    }

    public String getFixmedinsCode() {
        return fixmedinsCode;
    }

    public boolean isOpenExportInterface() {
        return isOpenExportInterface;
    }
    @Override
    public String toString() {
        return "AppConfig{" +
                "browserPort='" + browserPort + '\'' +
                ", insuType='" + insuType + '\'' +
                ", insuTypeCn='" + insuTypeCn + '\'' +
                ", billUrl='" + billUrl + '\'' +
                ", token='" + (token != null && token.length() > 5 ?
                token.substring(0, 3) + "***" : "***") + '\'' + // 敏感信息脱敏
                ", session='" + session + '\'' +
                ", pageSize='" + pageSize + '\'' +
                ", frontUrl='" + frontUrl + '\'' +
                ", fixmedinsCode='" + fixmedinsCode + '\'' +
                '}';
    }

}