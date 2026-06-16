package com.reports.service;

import com.reports.dto.common.PageResult;
import com.reports.dto.request.CashDischargeSettlementRequest;
import com.reports.dto.response.cash.discharge.settlement.ChartsData;
import com.reports.dto.response.cash.discharge.settlement.OverviewData;
import com.reports.dto.response.cash.discharge.settlement.TableItem;

/**
 * 出院结算报表服务
 */
public interface CashDischargeSettlementService {

    /**
     * 查询概览数据
     */
    OverviewData queryOverview(CashDischargeSettlementRequest request);

    /**
     * 查询图表分析数据
     */
    ChartsData queryCharts(CashDischargeSettlementRequest request);

    /**
     * 查询表格数据（分页）
     */
    PageResult<TableItem> queryTable(CashDischargeSettlementRequest request, Integer page, Integer pageSize);

}
