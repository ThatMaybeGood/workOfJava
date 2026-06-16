package com.reports.dto.response.cash.cashier.settlement;

import lombok.Data;

/**
 * 收费员结账统计 - 概览数据
 */
@Data
public class OverviewData {

    private Integer appointmentRegister;
    private Integer appointmentRegisterCompare;
    private Integer appointmentFetch;
    private Integer appointmentFetchCompare;
    private Integer todayRegister;
    private Integer todayRegisterCompare;
    private Integer refund;
    private Integer refundCompare;
    private Integer outpatientCharge;
    private Integer outpatientChargeCompare;
    private Integer outpatientRefund;
    private Integer outpatientRefundCompare;
    private Integer prepayment;
    private Integer prepaymentCompare;
    private Integer hospitalRefund;
    private Integer hospitalRefundCompare;
    private Integer dischargeSettlement;
    private Integer dischargeSettlementCompare;

}
