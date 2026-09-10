package com.reports.dto.response.outpatient.alert;

import lombok.Data;

/**
 * 门诊预警统计 - 明细行数据
 */
@Data
public class DetailItem {

    private String deptName;
    private String doctorName;
    private String statDate;
    /** 出诊时间段 */
    private String clinicPeriod;
    /** HIS工作站最后登出时间 */
    private String hisLogoutTime;
    private Integer remainAlert;
    private Integer appointmentAlert;
    private Integer earlyLeave;
}
