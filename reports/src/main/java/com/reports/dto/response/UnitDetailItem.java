package com.reports.dto.response;

import lombok.Data;

/**
 * 单元明细项（有效/总数）
 */
@Data
public class UnitDetailItem {

    /**
     * 有效数
     */
    private Integer effective;

    /**
     * 总数
     */
    private Integer total;

}
