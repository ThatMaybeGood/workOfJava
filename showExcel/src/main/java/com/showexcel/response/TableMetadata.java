package com.showexcel.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2025/11/4 17:37
 */
/**
 * 表格元数据DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableMetadata {

    /**
     * 总行数
     */
    private Integer totalRows;

    /**
     * 总列数
     */
    private Integer totalCols;

    /**
     * 生成时间
     */
    private LocalDateTime generatedAt;

    /**
     * 报表日期
     */
    private LocalDate reportDate;

    // constructors, getters, setters...
}