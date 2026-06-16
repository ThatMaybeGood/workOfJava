package com.reports.dto.response.outpatient.alert;

import lombok.Data;

/**
 * 门诊预警统计 - 科室表格行数据
 */
@Data
public class DeptTableItem {

    private String deptName;
    private Integer remainAlert;
    private Integer appointmentAlert;
    private Integer earlyLeave;

}
