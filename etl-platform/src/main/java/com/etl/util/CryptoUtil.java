package com.etl.util;

import com.ulisesbocchio.jasyptspringboot.properties.JasyptEncryptorConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

@Slf4j
public class CryptoUtil {

    private static PooledPBEStringEncryptor encryptor;

    public static void init(String password) {
        encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(password);
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setIvGeneratorClassName("org.jasypt.iv.NoIvGenerator");
        config.setPoolSize(1);
        encryptor.setConfig(config);
    }

    public static String encrypt(String value) {
        if (encryptor == null) {
            throw new IllegalStateException("CryptoUtil未初始化");
        }
        try {
            return "ENC(" + encryptor.encrypt(value) + ")";
        } catch (Exception e) {
            log.error("加密失败", e);
            throw new RuntimeException("加密失败", e);
        }
    }

    public static String decrypt(String value) {
        if (encryptor == null) {
            throw new IllegalStateException("CryptoUtil未初始化");
        }
        try {
            if (value != null && value.startsWith("ENC(") && value.endsWith(")")) {
                String encrypted = value.substring(4, value.length() - 1);
                return encryptor.decrypt(encrypted);
            }
            return value;
        } catch (Exception e) {
            log.error("解密失败", e);
            throw new RuntimeException("解密失败", e);
        }
    }
}
