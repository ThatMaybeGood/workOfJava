package com.reports.dto.response.outpatient.revenue;

import lombok.Data;

/**
 * 门诊收入分析 - 科室表格行数据
 */
@Data
public class DeptTableItem {

    private String deptName;
    private String outpatientRevenue;
    private String serviceRevenue;

}
