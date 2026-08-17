package com.reports.etl.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class DsEncryptUtil {

    private static final String KEY = "etlDsMetaKey2026";

    private DsEncryptUtil() {}

    public static String encrypt(String plain) {
        if (plain == null) return null;
        try {
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(plain.getBytes("UTF-8")));
        } catch (Exception e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }

    public static String decrypt(String encrypted) {
        if (encrypted == null) return null;
        try {
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decoded = Base64.getDecoder().decode(encrypted);
            return new String(cipher.doFinal(decoded), "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("密码解密失败", e);
        }
    }
}