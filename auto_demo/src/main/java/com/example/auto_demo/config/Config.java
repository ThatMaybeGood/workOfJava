package com.example.auto_demo.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@ConfigurationProperties(prefix = "yb")
@EnableConfigurationProperties(Config.class)
public class Config {


    private String insuType;
    private String insuTypeCn;

    private String billUrl;

    private String token;

    private String session;

    private String pageSize;

    private String frontUrl;


    public String getInsuType() {
        return insuType;
    }

    public void setInsuType(String insuType) {
        this.insuType = insuType;
    }

    public String getInsuTypeCn() {
        return insuTypeCn;
    }

    public void setInsuTypeCn(String insuTypeCn) {
        this.insuTypeCn = insuTypeCn;
    }

    public String getBillUrl() {
        return billUrl;
    }

    public void setBillUrl(String billUrl) {
        this.billUrl = billUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getPageSize() {
        return pageSize;
    }

    public void setPageSize(String pageSize) {
        this.pageSize = pageSize;
    }

    public String getFrontUrl() {
        return frontUrl;
    }

    public void setFrontUrl(String frontUrl) {
        this.frontUrl = frontUrl;
    }

    public Config() {
        System.out.println("构造函数执行中...");
    }

    public Config(String insuType, String insuTypeCn, String billUrl, String token, String session, String pageSize, String frontUrl) {

        this.insuType = insuType;
        this.insuTypeCn = insuTypeCn;
        this.billUrl = billUrl;
        this.token = token;
        this.session = session;
        this.pageSize = pageSize;
        this.frontUrl = frontUrl;
    }

    @PostConstruct
    public void init() {
        // 依赖注入已经完成，appName 有值了
        System.out.println("在 @PostConstruct 中获取的 insuType: " + insuType);
      //  return new AppConfig(browserPort, insuType, insuTypeCn, billUrl, token, session, pageSize, frontUrl);
    }



    @Override
    public String toString() {
        return "AppConfig{" +
                ", insuType='" + insuType + '\'' +
                ", insuTypeCn='" + insuTypeCn + '\'' +
                ", billUrl='" + billUrl + '\'' +
                ", token='" + token + '\'' +
                ", session='" + session + '\'' +
                ", pageSize='" + pageSize + '\'' +
                ", frontUrl='" + frontUrl + '\'' +
                '}';
    }

    public static void main(String[] args) {
    	Config appConfig = new Config();
    	System.out.println(appConfig.toString());
    }
}
