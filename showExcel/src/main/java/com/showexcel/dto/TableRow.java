package com.showexcel.dto;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2025/11/4 17:48
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 表格数据行DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableRow {

    /**
     * 行ID（数据库主键，可选）
     */
    private Long id;

    /**
     * 行索引（在表格中的位置）
     */
    private Integer index;

    /**
     * 行类型
     */
    private RowType type;

    /**
     * 行名称/标识
     */
    private String name;

    /**
     * 业务数据
     */
    private RowData data;

    /**
     * 备注信息
     */
    private String remarks;

    // constructors, getters, setters...
}

/**
 * 行类型枚举
 */
