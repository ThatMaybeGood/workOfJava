package com.reports.dto.response.outpatient.quality.control;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 门诊质控指标维护项
 */
@Data
public class QcMaintainItem {

    /**
     * 指标编码（对应 tr_qc_dtl 列名）
     */
    private String indicatorCode;

    /**
     * 指标名称
     */
    private String indicatorName;

    /**
     * 分子值
     */
    private BigDecimal numerator;

    /**
     * 分母值
     */
    private BigDecimal denominator;
}
