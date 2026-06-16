package com.reports.dto.response.outpatient.med.tech;

import lombok.Data;

/**
 * 医技统计 - 概览数据
 */
@Data
public class OverviewData {

    private Integer checkCount;
    private String onTimeRate;
    private String waitTime;
    private String avgWaitLate;
    private String avgReportTime;

}
