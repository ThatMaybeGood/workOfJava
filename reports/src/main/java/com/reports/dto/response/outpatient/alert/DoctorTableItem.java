package com.reports.dto.response.outpatient.alert;

import lombok.Data;

/**
 * 门诊预警统计 - 医生表格行数据
 */
@Data
public class DoctorTableItem {

    private String doctorName;
    private String deptName;
    private Integer remainAlert;
    private Integer appointmentAlert;
    private Integer earlyLeave;

}
