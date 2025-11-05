package com.showexcel.response;

/**
 * 分区类型枚举
 */
public enum SectionType {
    ACCOUNTING("accounting", "会计数据"),
    RESERVATION("reservation", "预约数据"),
    GRAND_TOTAL("grand_total", "总计"),
    OTHER("other", "其他");

    private final String code;
    private final String description;

    SectionType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static SectionType fromCode(String code) {
        for (SectionType type : SectionType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return OTHER;
    }
}
