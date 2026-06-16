package com.reports.dto.response.outpatient.med.tech;

import lombok.Data;

/**
 * 医技统计 - 表格行数据
 */
@Data
public class TableItem {

    private String deptName;
    private Integer checkCount;
    private String onTimeRate;
    private Double waitTime;
    private Double avgWaitLate;
    private Double avgReportTime;

}
