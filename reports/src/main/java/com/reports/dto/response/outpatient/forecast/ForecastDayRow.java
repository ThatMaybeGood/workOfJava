package com.reports.dto.response.outpatient.forecast;

import lombok.Data;

/**
 * 按日聚合行(门诊量/预约量/天气系数)
 */
@Data
public class ForecastDayRow {

    /** 日期 yyyy-MM-dd */
    private String dd;

    /** 当日总挂号行数 */
    private Double num;

    /** 当日预约行数(indicator=1 或 indicator=2且挂号日!=就诊日, 仅门诊量按日聚合使用) */
    private Double appoint;
}
