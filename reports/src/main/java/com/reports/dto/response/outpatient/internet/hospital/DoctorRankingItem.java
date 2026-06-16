package com.reports.dto.response.outpatient.internet.hospital;

import lombok.Data;

/**
 * 互医质控运营月报 - 医生排行数据
 */
@Data
public class DoctorRankingItem {

    private Integer rank;
    private String doctorName;
    private String deptName;
    private String title;
    private Integer currentMonth;

}
