package com.showexcel.response;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2025/11/4 17:49
 */


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 表格布局配置DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableLayout {


    /**
     * 样式类型
     */
    private RowType style;


    /**
     * 特殊单元格配置
     */
    private List<LayoutCell> specialCells;

    // constructors, getters, setters...
}

/**
 * 单元格样式枚举
 */
