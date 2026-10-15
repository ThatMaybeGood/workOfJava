package com.reports.dto.response.cash.cashier.settlement;

import lombok.Data;

/**
 * 收费员结账统计 - 概览挂号类指标（源表 TR_OUTP_FIN_CLINIC_MASTER 本期/同期计数）
 */
@Data
public class RegisterOverviewData {

    /** 预约挂号量（本期，按STAT_DATE归期） */
    private Integer appointmentRegister;

    /** 预约挂号量（去年同期） */
    private Integer appointmentRegisterLast;

    /** 预约取号量（本期，按VISIT_DATE归期） */
    private Integer appointmentFetch;

    /** 预约取号量（去年同期） */
    private Integer appointmentFetchLast;

    /** 当日挂号量（本期，按STAT_DATE归期） */
    private Integer todayRegister;

    /** 当日挂号量（去年同期） */
    private Integer todayRegisterLast;

    /** 退号量（本期，按RETURNED_DATE归期） */
    private Integer refund;

    /** 退号量（去年同期） */
    private Integer refundLast;

}
