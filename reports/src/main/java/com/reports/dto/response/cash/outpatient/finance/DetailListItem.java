package com.reports.dto.response.cash.outpatient.finance;

import lombok.Data;

/**
 * 门诊财务报表 - 明细列表项（日期维度）
 */
@Data
public class DetailListItem {

    private static final long serialVersionUID = 1L;

    /** 日期（月模式 yyyy-MM / 天模式 yyyy-MM-dd） */
    private String dateTime;

    /** 去年同期门诊量 */
    private Double lastYearOutpatientVolume;

    /** 当前日期门诊量 */
    private Double currentDateOutpatientVolume;

    /** 去年同期缴费人次 */
    private Double lastYearNumberCharges;

    /** 当前日期缴费人次 */
    private Double currentDateNumberCharges;

    /** 去年同期收据张数 */
    private Double lastYearNumberReceipt;

    /** 当前日期收据张数 */
    private Double currentDateNumberReceipt;

    /** 去年同期金额 */
    private Double lastYearAmount;

    /** 当前日期金额 */
    private Double currentDateAmount;

}
