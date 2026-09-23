package com.reports.dto.response.outpatient.forecast;

import lombok.Data;

/**
 * 预测门诊量报表 - 概览数据
 * 四个值都由 OutpatientForecastServiceImpl 实时算出,非查表
 */
@Data
public class OverviewData {

    /** 明日预测门诊量 = 30天日预测的第1天 */
    private Integer tomorrow;
    /** 未来一周预测门诊量 = 日预测第1~7天求和 */
    private Integer nextWeek;
    /** 未来一月预测门诊量 = 日预测全部30天求和 */
    private Integer nextMonth;
    /** 未来一年预测门诊量 = 当前月起12个月预测求和(跨年) */
    private Integer nextYear;

}
