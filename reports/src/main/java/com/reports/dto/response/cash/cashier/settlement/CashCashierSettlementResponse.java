package com.reports.dto.response.cash.cashier.settlement;

import com.reports.dto.common.PageResult;
import lombok.Data;

/**
 * 收费员结账统计 - 响应体
 */
@Data
public class CashCashierSettlementResponse {

    private static final long serialVersionUID = 1L;

    /**
     * 概览数据
     */
    private OverviewData overview;

    /**
     * 表格分页数据
     */
    private PageResult<TableItem> table;

    /**
     * 图表数据
     */
    private ChartData chart;

}
