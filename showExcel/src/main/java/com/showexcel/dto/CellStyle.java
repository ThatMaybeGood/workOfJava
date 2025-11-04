package com.showexcel.dto;

/**
 * 单元格样式枚举
 */
public enum CellStyle {
    SECTION_HEADER("section_header", "区域标题"),
    SUMMARY_LABEL("summary_label", "汇总标签"),
    SPECIAL_LABEL("special_label", "特殊标签"),
    SIGNATURE("signature", "签名区域"),
    DATA_CELL("data_cell", "数据单元格");

    private final String code;
    private final String description;

    CellStyle(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static CellStyle fromCode(String code) {
        for (CellStyle style : CellStyle.values()) {
            if (style.getCode().equals(code)) {
                return style;
            }
        }
        return DATA_CELL;
    }
}
