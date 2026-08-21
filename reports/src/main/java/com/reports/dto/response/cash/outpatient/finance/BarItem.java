package com.reports.dto.response.cash.outpatient.finance;

import lombok.Data;

/**
 * 门诊财务报表 - 柱状图数据项
 */
@Data
public class BarItem {

    private static final long serialVersionUID = 1L;

    /** 日期（月模式 yyyy-MM / 天模式 yyyy-MM-dd） */
    private String dateTime;

    /** 去年同期值 */
    private Double lastYearNumber;

    /** 当前日期值 */
    private Double currentDateNumber;

}
