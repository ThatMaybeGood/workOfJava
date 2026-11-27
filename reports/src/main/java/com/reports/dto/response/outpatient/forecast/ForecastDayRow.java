package com.reports.dto.response.outpatient.forecast;

import lombok.Data;

/**
 * 按日聚合行(门诊量/预约量/天气系数)
 */
@Data
public class ForecastDayRow {

    /** 日期 yyyy-MM-dd */
    private String dd;

    /** 当日门诊量 */
    private Double num;

}
