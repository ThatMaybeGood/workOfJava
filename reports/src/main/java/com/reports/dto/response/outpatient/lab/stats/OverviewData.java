package com.reports.dto.response.outpatient.lab.stats;

import lombok.Data;

/**
 * 检验统计 - 概览数据
 */
@Data
public class OverviewData {

    private Integer bloodCollection;
    private String bloodEfficiency;
    private String labEfficiency;

}
