package com.reports.dto.response.cash.discharge.settlement;

import lombok.Data;

/**
 * 出院结算报表 - 表格行数据
 */
@Data
public class TableItem {

    private String date;
    private Integer totalLast;
    private Integer totalCurrent;
    private Integer totalCompare;
    private Integer dischargedLast;
    private Integer dischargedCurrent;
    private Integer dischargedCompare;
    private Integer notDischargedLast;
    private Integer notDischargedCurrent;
    private Integer notDischargedCompare;
    private Double amountLast;
    private Double amountCurrent;
    private Integer amountCompare;

}
