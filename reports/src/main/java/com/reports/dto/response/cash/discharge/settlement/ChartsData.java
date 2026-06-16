package com.reports.dto.response.cash.discharge.settlement;

import lombok.Data;
import java.util.List;

/**
 * 出院结算报表 - 图表分析数据
 */
@Data
public class ChartsData {

    private List<ChartItem> channelAnalysis;
    private List<ChartItem> patientTypeAnalysis;
    private List<ChartItem> amountTypeAnalysis;

}
