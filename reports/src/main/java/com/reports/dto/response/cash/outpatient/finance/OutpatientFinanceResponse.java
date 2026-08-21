package com.reports.dto.response.cash.outpatient.finance;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 门诊财务报表 - 响应体（复合返回）
 */
@Data
public class OutpatientFinanceResponse {

    private static final long serialVersionUID = 1L;

    /**
     * 指标卡片数据
     */
    private IndicatorData indicator;

    /**
     * 明细列表（日期维度）
     */
    private List<DetailListItem> detailList;

    /**
     * 柱状图数据：business_type(1~4) -> 柱图序列
     */
    private Map<String, List<BarItem>> barList;

    /**
     * 饼状图数据：business_type(1~10) -> 饼图序列
     */
    private Map<String, List<PieItem>> pieList;

}
