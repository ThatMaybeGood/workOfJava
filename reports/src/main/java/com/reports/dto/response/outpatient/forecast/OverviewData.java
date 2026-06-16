package com.reports.dto.response.outpatient.forecast;

import lombok.Data;

/**
 * 预测门诊量报表 - 概览数据
 */
@Data
public class OverviewData {

    private Integer tomorrow;
    private Integer nextWeek;
    private Integer nextMonth;
    private Integer nextYear;

}
