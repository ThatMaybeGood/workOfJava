package com.reports.service;

import com.reports.dto.common.PageResult;
import com.reports.dto.request.CashCashierSettlementRequest;
import com.reports.dto.response.cash.cashier.settlement.ChartData;
import com.reports.dto.response.cash.cashier.settlement.OverviewData;
import com.reports.dto.response.cash.cashier.settlement.TableItem;

/**
 * 收费员结账统计服务
 */
public interface CashCashierSettlementService {

    /**
     * 查询概览数据
     */
    OverviewData queryOverview(CashCashierSettlementRequest request);

    /**
     * 查询表格数据（分页）
     */
    PageResult<TableItem> queryTable(CashCashierSettlementRequest request, Integer page, Integer pageSize);

    /**
     * 查询图表数据
     */
    ChartData queryChart(CashCashierSettlementRequest request);

}
