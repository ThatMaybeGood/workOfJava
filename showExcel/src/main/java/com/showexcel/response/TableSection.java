package com.showexcel.response;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2025/11/4 17:46
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 表格分区DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableSection {

    /**
     * 分区名称（如：会计室、预约中心）
     */
    private String name;

    /**
     * 分区类型
     */
    private SectionType type;
     /**
     * 分区内行数
     */
    private Integer rowCount;

    /**
     * 分区内的数据行
     */
    private List<RowData> rows;

    // constructors, getters, setters...
}

/**
 * 分区类型枚举
 */
