package com.reports.dto.response.outpatient.quality.control;

import lombok.Data;

/**
 * 门诊管理质量控制 - 概览数据
 */
@Data
public class OverviewData {

    private String emrUsageRate;
    private String standardDiagnosisRate;
    private String onTimeRate;
    private String stopRate;
    private String chemoRecordRate;
    private String chemoAdverseRate;
    private String chemoInfusionRate;
    private String criticalValueRate;
    private String bloodDrawErrorRate;
    private String surgeryComplicationRate;
    private String adverseEventRate;

}
