package com.example.auto_demo.util;

public enum InsuType {

    // 枚举定义
    EMPLOYEE_BASIC("310", "职工基本医疗保险"),
    RESIDENT_BASIC("390", "城乡居民基本医疗保险"),
    MATERNITY("515253", "生育医疗保险"),
    EMPLOYEE_REMOTE("3101", "异地职工基本医疗保险"),
    RESIDENT_REMOTE("3901", "异地居民基本医疗保险");

    private String code;
    private String name;

    InsuType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String nameOf(String code) {
        for (InsuType t : values()) {
            if (t.code.equals(code)) return t.name;
        }
        return "";
    }
}
