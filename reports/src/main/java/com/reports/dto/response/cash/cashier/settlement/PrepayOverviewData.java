package com.reports.dto.response.cash.cashier.settlement;

import lombok.Data;

/**
 * 收费员结账统计 - 概览预交金类指标（源表 tr_inpat_prepay_rcpt 本期/同期计数）
 */
@Data
public class PrepayOverviewData {

    /** 收预交金量（本期，transact_type=交款） */
    private Integer prepayment;

    /** 收预交金量（去年同期） */
    private Integer prepaymentLast;

    /** 退院量（本期，transact_type=退款） */
    private Integer hospitalRefund;

    /** 退院量（去年同期） */
    private Integer hospitalRefundLast;

}
