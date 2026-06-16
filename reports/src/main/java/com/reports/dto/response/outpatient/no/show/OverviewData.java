package com.reports.dto.response.outpatient.no.show;

import lombok.Data;

/**
 * 爽约退号分析 - 概览数据
 */
@Data
public class OverviewData {

    private Integer refundCount;
    private String refundRate;
    private Integer noShowCount;
    private String noShowRate;

}
