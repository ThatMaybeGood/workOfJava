package com.showexcel.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 布局单元格DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
     * 行合并数
     */
    private Integer rowSpan;
    /**
     * 列合并数
     */
    private Integer colSpan;

    /**
     * 显示内容
     */
    private String content;

    // constructors, getters, setters...
}
