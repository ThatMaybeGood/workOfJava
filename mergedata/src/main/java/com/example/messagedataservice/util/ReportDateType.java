package com.example.messagedataservice.util;

import java.util.stream.Stream;

/**
 * 报表日期类型枚举
 */
public enum ReportDateType {

    /** 0: 正常日 */
    NORMAL(0, "正常日"),

    /** 1: 节假日 */
    HOLIDAY(1, "节假日"),

    /** 2: 节假日前一天 (通常指的是连假开始前一天) */
    PRE_HOLIDAY(2, "节假日前一天"),

    /** 3: 节假日后一天 (通常指的是连假结束后的第一天) */
    POST_HOLIDAY(3, "节假日后一天");

    // 内部字段
    private final int code;
    private final String description;

    // 构造函数
    ReportDateType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    // Getter 方法
    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据Code查找对应的枚举 (方便外部通过数字使用)
     * @param code 日期类型代码
     * @return 对应的 ReportDateType，如果找不到则返回 NORMAL
     */
    public static ReportDateType fromCode(int code) {
        return Stream.of(ReportDateType.values())
                .filter(type -> type.code == code)
                .findFirst()
                .orElse(NORMAL);
    }
}

