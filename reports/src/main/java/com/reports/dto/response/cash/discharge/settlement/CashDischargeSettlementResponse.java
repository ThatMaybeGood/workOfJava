package com.reports.dto.response.cash.discharge.settlement;

import com.reports.dto.common.PageResult;
import lombok.Data;

/**
 * 出院结算报表 - 响应体
 */
@Data
public class CashDischargeSettlementResponse {

    private static final long serialVersionUID = 1L;

    /**
     * 概览数据
     */
    private OverviewData overview;

    /**
     * 图表分析数据
     */
    private ChartsData charts;

    /**
     * 表格分页数据
     */
    private PageResult<TableItem> table;

}
