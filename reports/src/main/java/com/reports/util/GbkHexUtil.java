package com.reports.util;

/**
 * US7ASCII库中文处理: RAWTOHEX读回Java按GBK解码 / 关键字转GBK大写hex供RAWTOHEX(列) LIKE匹配
 */
public class GbkHexUtil {

    private GbkHexUtil() {
    }

    /** RAWTOHEX读回的hex还原为字节按GBK解码 */
    public static String decode(String hex) {
        if (hex == null) {
            return null;
        }
        try {
            byte[] bytes = new byte[hex.length() / 2];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
            }
            return new String(bytes, "GBK").trim();
        } catch (Exception e) {
            return hex;
        }
    }

    /** 关键字转GBK字节的大写hex; GBK双字节高字节均>=0x81, hex串纯ASCII可安全拼入SQL */
    public static String encodeHex(String text) {
        if (text == null) {
            return "";
        }
        try {
            StringBuilder sb = new StringBuilder();
            for (byte b : text.getBytes("GBK")) {
                sb.append(String.format("%02X", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
