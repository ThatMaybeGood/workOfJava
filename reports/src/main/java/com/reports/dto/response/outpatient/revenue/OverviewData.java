package com.reports.dto.response.outpatient.revenue;

import lombok.Data;

/**
 * 门诊收入分析 - 概览数据
 */
@Data
public class OverviewData {

    private Double outpatientRevenue;
    private Double serviceRevenue;

}
