package com.example.auto_demo.util;

public class StringUtil {
    public static String getSessionId(){
        String sessionId = String.valueOf(System.currentTimeMillis()) + "|";
        return sessionId;
    }
}
