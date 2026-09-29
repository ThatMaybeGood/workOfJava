package com.reports.service;

import com.reports.dto.common.PageResult;
import com.reports.dto.request.CashDischargeSettlementRequest;
import com.reports.dto.response.cash.discharge.settlement.ChartItem;
import com.reports.dto.response.cash.discharge.settlement.ChartsData;
import com.reports.dto.response.cash.discharge.settlement.OverviewData;
import com.reports.dto.response.cash.discharge.settlement.TableItem;

import java.util.List;

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
     * 查询图表钻取明细：渠道→费别人次、支付方式→收退占比
     */
    List<ChartItem> queryChartDetail(CashDischargeSettlementRequest request);

    /**
     * 查询表格数据（分页）
     */
    PageResult<TableItem> queryTable(CashDischargeSettlementRequest request, Integer page, Integer pageSize);

}
