package com.reports.service;

import com.reports.dto.request.OutpatientFinanceRequest;
import com.reports.dto.response.cash.outpatient.finance.BarItem;
import com.reports.dto.response.cash.outpatient.finance.DetailListItem;
import com.reports.dto.response.cash.outpatient.finance.IndicatorData;
import com.reports.dto.response.cash.outpatient.finance.PieItem;

import java.util.List;
import java.util.Map;

/**
 * 门诊财务报表服务
 */
public interface OutpatientFinanceService {

    /**
     * 查询指标卡片数据（从已计算的明细列表聚合）
     */
    IndicatorData queryIndicator(OutpatientFinanceRequest request, List<DetailListItem> detailList);

    /**
     * 查询指标卡片数据
     */
    IndicatorData queryIndicator(OutpatientFinanceRequest request);

    /**
     * 查询明细列表数据
     */
    List<DetailListItem> queryDetailList(OutpatientFinanceRequest request);

    /**
     * 查询柱状图数据（business_type 1~4）（从已计算的明细列表组装）
     */
    Map<String, List<BarItem>> queryBarList(OutpatientFinanceRequest request, List<DetailListItem> detailList);

    /**
     * 查询柱状图数据（business_type 1~4）
     */
    Map<String, List<BarItem>> queryBarList(OutpatientFinanceRequest request);

    /**
     * 查询饼状图数据（business_type 1~10）
     */
    Map<String, List<PieItem>> queryPieList(OutpatientFinanceRequest request);

}
