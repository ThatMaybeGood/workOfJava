package com.reports.dto.response.cash.outpatient.finance;

import lombok.Data;

/**
 * 门诊财务报表 - 指标卡片
 */
@Data
public class IndicatorData {

    private static final long serialVersionUID = 1L;

    /** 门诊量 */
    private Double outpatientVolume;

    /** 缴费人次 */
    private Double numberCharges;

    /** 收据张数 */
    private Double numberReceipt;

    /** 金额（汇总=净收入/进项=收入/退项=退费） */
    private Double amount;

    /** 门诊量同比 */
    private String outpatientVolumeYoy;

    /** 缴费人次同比 */
    private String numberChargesYoy;

    /** 收据张数同比 */
    private String numberReceiptYoy;

    /** 金额同比 */
    private String amountYoy;

}
