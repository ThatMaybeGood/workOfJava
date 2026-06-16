package com.reports.dto.response.outpatient.specialty.treatment;

import lombok.Data;

/**
 * 专科治疗量统计 - 概览数据
 */
@Data
public class OverviewData {

    private Integer treatmentCount;
    private Double treatmentAmount;
    private Integer patientCount;

}
