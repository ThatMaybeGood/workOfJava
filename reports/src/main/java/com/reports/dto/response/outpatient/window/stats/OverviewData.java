package com.reports.dto.response.outpatient.window.stats;

import lombok.Data;

/**
 * 人工窗口统计 - 概览数据
 */
@Data
public class OverviewData {

    private Integer registerCount;
    private Integer paymentCount;
    private Integer refundCount;

}
