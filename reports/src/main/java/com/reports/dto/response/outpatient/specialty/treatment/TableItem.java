package com.reports.dto.response.outpatient.specialty.treatment;

import lombok.Data;

/**
 * 专科治疗量统计 - 表格行数据
 */
@Data
public class TableItem {

    private String deptName;
    private Integer treatmentCount;
    private Double treatmentAmount;
    private Integer patientCount;

}
