package com.example.auto_demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class DynamicConfig {

    @Autowired
    private AdminConfigProperties adminConfigProperties;

    private volatile String token;
    private volatile String session;

    @PostConstruct
    public void init() {
        this.token = adminConfigProperties.getToken();
        this.session = adminConfigProperties.getSession();
    }

    public synchronized void setToken(String token) {
        this.token = token;
    }

    public synchronized void setSession(String session) {
        this.session = session;
    }

    public String getToken() {
        return token;
    }

    public String getSession() {
        return session;
    }
}
