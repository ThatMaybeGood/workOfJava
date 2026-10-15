package com.reports.dto.response.cash.cashier.settlement;

import lombok.Data;

/**
 * 收费员结账统计 - 概览收费类指标（源表 TR_OUTP_FIN_RCPT_ACCT 本期/同期计数）
 */
@Data
public class RcptOverviewData {

    /** 门诊收费量（本期，进项：NVL(TOTAL_CHARGES,0)>=0） */
    private Integer outpatientCharge;

    /** 门诊收费量（去年同期） */
    private Integer outpatientChargeLast;

    /** 门诊退费量（本期，退项：REFUNDED_RCPT_NO IS NOT NULL AND TOTAL_CHARGES<0） */
    private Integer outpatientRefund;

    /** 门诊退费量（去年同期） */
    private Integer outpatientRefundLast;

}
