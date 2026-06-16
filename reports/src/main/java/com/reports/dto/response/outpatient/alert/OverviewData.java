package com.reports.dto.response.outpatient.alert;

import lombok.Data;

/**
 * 门诊预警统计 - 概览数据
 */
@Data
public class OverviewData {

    private Integer remainAlert;
    private Integer appointmentAlert;
    private Integer earlyLeave;

}
