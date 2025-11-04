package com.showexcel.dto;

/**
 * 行类型枚举
 */
public enum RowType {
    DATA("data", "数据行"),
    SUMMARY("summary", "汇总行"),
    TOTAL("total", "总计行"),
    PLACEHOLDER("placeholder", "占位行");

    private final String code;
    private final String description;

    RowType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static RowType fromCode(String code) {
        for (RowType type : RowType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return DATA;
    }
}
