package com.reports.dto.response.cash.cashier.settlement;

import lombok.Data;

/**
 * 收费员结账统计 - 概览出院结算指标（源表 tr_disch_settle_visit 本期/同期计数）
 */
@Data
public class DischargeOverviewData {

    /** 出院结算量（本期，settle_status=1 已出院结算） */
    private Integer dischargeSettlement;

    /** 出院结算量（去年同期） */
    private Integer dischargeSettlementLast;

}
