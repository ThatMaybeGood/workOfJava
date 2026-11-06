package com.reports.dto.response.cash.mi;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 退费跨月配对明细(分类明细弹窗跨月匹配Tab): 退费↔HIS正交易逐条对应
 * 查找范围: 上月月初 ~ 退费当天(正交易bill_date <= 退费bill_date)
 */
@Data
public class MiReconPayCrossItem {

    /** 退费结算号 */
    private String refundSetlId;

    /** 退费日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date refundDate;

    /** 退费金额(负) */
    private BigDecimal refundFee;

    /** 匹配状态: 本月匹配/跨月匹配/找不到(SQL直出中文) */
    private String matchStatus;

    /** 正交易结算号(找不到为null) */
    private String posSetlId;

    /** 正交易日期(找不到为null) */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date posDate;

    /** 正交易月份 yyyy-MM(找不到为null) */
    private String posMonth;

    /** 正交易金额(找不到为null) */
    private BigDecimal posFee;
}
