package com.reports.dto.response.cash.outpatient.finance;

import lombok.Data;

/**
 * 门诊财务报表 - 饼状图数据项
 */
@Data
public class PieItem {

    private static final long serialVersionUID = 1L;

    /** 类别名称 */
    private String name;

    /** 当前值 */
    private Double currValue;

    /** 同比基期值 */
    private Double prevValue;

}
