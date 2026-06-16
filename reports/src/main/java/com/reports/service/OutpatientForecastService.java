package com.reports.service;

import com.reports.dto.request.OutpatientForecastRequest;
import com.reports.dto.response.outpatient.forecast.MonthForecast;
import com.reports.dto.response.outpatient.forecast.OverviewData;
import com.reports.dto.response.outpatient.forecast.YearForecast;

/**
 * 预测门诊量报表服务
 */
public interface OutpatientForecastService {

    /**
     * 查询概览数据
     */
    OverviewData queryOverview(OutpatientForecastRequest request);

    /**
     * 查询未来30天预测
     */
    MonthForecast queryMonthForecast(OutpatientForecastRequest request);

    /**
     * 查询未来12个月预测
     */
    YearForecast queryYearForecast(OutpatientForecastRequest request);

}
