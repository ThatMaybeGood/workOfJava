package com.showexcel.response;

/**
 * 行类型枚举
 */
public enum RowType {

    NO_MERGED_DATA("NO_MERGED_DATA", "无合并原始数据"),
    SINGLE_SINGLE_CELL_MERGED_DATA("single_single_cell_merged_data", "单行单个元素合并数据"),
    SINGLE_MULTI_CELL_MERGED_DATA("single_multi_cell_merged_data", "单行多个元素合并数据"),
    MULTIROWSCOLS_SINGLE_CELL_MERGED_DATA("multirowscols_single_cell_merged_data", "多行多列单个元素合并数据");

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

//    public static RowType fromCode(String code) {
//        for (RowType type : RowType.values()) {
//            if (type.getCode().equals(code)) {
//                return type;
//            }
//        }
//        return DATA;
//    }
}
