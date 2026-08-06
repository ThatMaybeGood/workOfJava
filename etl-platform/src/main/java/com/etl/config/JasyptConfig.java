package com.etl.config;

import com.ulisesbocchio.jasyptspringboot.properties.JasyptEncryptorConfigurationProperties;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import com.etl.util.CryptoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Slf4j
@Configuration
@EnableEncryptableProperties
public class JasyptConfig {

    @Value("${jasypt.encryptor.password:etlDefaultKey}")
    private String encryptorPassword;

    @PostConstruct
    public void init() {
        CryptoUtil.init(encryptorPassword);
        log.info("Jasypt加密工具初始化完成");
    }
}
