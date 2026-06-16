package com.reports.dto.response.outpatient.forecast;

import lombok.Data;

/**
 * 预测门诊量报表 - 响应体
 */
@Data
public class OutpatientForecastResponse {

    private static final long serialVersionUID = 1L;

    /**
     * 概览数据
     */
    private OverviewData overview;

    /**
     * 未来30天预测
     */
    private MonthForecast monthForecast;

    /**
     * 未来12个月预测
     */
    private YearForecast yearForecast;

}
