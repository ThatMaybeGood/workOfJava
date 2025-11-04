package com.showexcel.dto;

/**
 * 布局单元格DTO
 */
public class LayoutCell {

    /**
     * 行位置（0-based）
     */
    private Integer row;

    /**
     * 列位置（0-based）
     */
    private Integer col;

    /**
     * 列合并数
     */
    private Integer colSpan;

    /**
     * 行合并数
     */
    private Integer rowSpan;

    /**
     * 显示内容
     */
    private String content;

    /**
     * 样式类型
     */
    private CellStyle style;

    // constructors, getters, setters...
}
