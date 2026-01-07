package com.example.auto_demo.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
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

    public String getBrowserPort() {
        return browserPort;
    }

    public void setBrowserPort(String browserPort) {
        this.browserPort = browserPort;
    }

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

    public String getFixmedinsCode() {
		return fixmedinsCode;
	}
    public String setFixmedinsCode(String fixmedinsCode) {
		this.fixmedinsCode = fixmedinsCode;
		return fixmedinsCode;
	}

    public AppConfig() {
        System.out.println("构造函数执行中...");
    }

    public AppConfig(String browserPort, String insuType, String insuTypeCn, String billUrl, String token, String session, String pageSize, String frontUrl,String fixmedinsCode) {
        this.browserPort = browserPort;
        this.insuType = insuType;
        this.insuTypeCn = insuTypeCn;
        this.billUrl = billUrl;
        this.token = token;
        this.session = session;
        this.pageSize = pageSize;
        this.frontUrl = frontUrl;
        this.fixmedinsCode = fixmedinsCode;
    }

    @PostConstruct
    public void init() {
        // 依赖注入已经完成，appName 有值了
        System.out.println("在 @PostConstruct 中获取的 insuType: " + insuType);
      //  return new AppConfig(browserPort, insuType, insuTypeCn, billUrl, token, session, pageSize, frontUrl);
    }
    @PostConstruct
    public  AppConfig getAppConfig() {
        return new AppConfig(browserPort, insuType, insuTypeCn, billUrl, token, session, pageSize, frontUrl, fixmedinsCode);
    }


    @Override
    public String toString() {
        return "AppConfig{" +
                "browserPort='" + browserPort + '\'' +
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
    	AppConfig appConfig = new AppConfig();
    	System.out.println(appConfig.toString());
    }
}
