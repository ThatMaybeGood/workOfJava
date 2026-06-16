package com.reports.dto.response.cash.discharge.settlement;

import lombok.Data;

/**
 * 出院结算报表 - 图表单项
 */
@Data
public class ChartItem {

    private String name;
    private Integer value;
    private Integer compare;

}
