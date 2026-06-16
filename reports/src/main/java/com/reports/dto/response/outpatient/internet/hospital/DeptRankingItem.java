package com.reports.dto.response.outpatient.internet.hospital;

import lombok.Data;

/**
 * 互医质控运营月报 - 科室排行数据
 */
@Data
public class DeptRankingItem {

    private Integer rank;
    private String deptName;
    private Integer currentMonth;
    private Integer lastMonth;
    private String growth;

}
