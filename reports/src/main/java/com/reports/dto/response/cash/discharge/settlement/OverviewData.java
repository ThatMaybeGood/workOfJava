package com.reports.dto.response.cash.discharge.settlement;

import lombok.Data;

/**
 * 出院结算报表 - 概览数据
 */
@Data
public class OverviewData {

    private Integer totalDischargeCount;
    private Integer totalDischargeCompare;
    private Integer dischargedCount;
    private Integer dischargedCompare;
    private Integer notDischargedCount;
    private Integer notDischargedCompare;
    private Double settlementAmount;
    private Integer settlementAmountCompare;

}
