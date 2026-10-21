package com.reports.dto.response.cash.prehosp;

import lombok.Data;

/**
 * 院前转住院统计行
 */
@Data
public class PreHospTransItem {

    /** 日期(按天yyyy-MM-dd/按月yyyy-MM) */
    private String itemDate;

    /** 操作员(user_id) */
    private String operNo;

    /** 操作员姓名(无字典回退工号) */
    private String operName;

    /** 收据张数 */
    private Integer rcptCnt;

    /** 人次(distinct patient_id) */
    private Integer personCnt;
}
