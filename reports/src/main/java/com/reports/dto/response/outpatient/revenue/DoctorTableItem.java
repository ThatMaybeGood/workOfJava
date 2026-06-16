package com.reports.dto.response.outpatient.revenue;

import lombok.Data;

/**
 * 门诊收入分析 - 医生表格行数据
 */
@Data
public class DoctorTableItem {

    private String doctorName;
    private String deptName;
    private String doctorBenefit;
    private String serviceRevenue;

}
