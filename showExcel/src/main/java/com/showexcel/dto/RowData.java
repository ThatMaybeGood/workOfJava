package com.showexcel.dto;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2025/11/4 17:47
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.function.Function;

/**
 * 行业务数据DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RowData {

    /**
     * 预交金收入
     */
    private BigDecimal hisAdvancePayment;

    /**
     * 医疗收入
     */
    private BigDecimal hisMedicalIncome;

    /**
     * 挂号收入
     */
    private BigDecimal hisRegistrationIncome;

    /**
     * 应交报表数
     */
    private BigDecimal reportAmount;

    /**
     * 前日暂收款
     */
    private BigDecimal previousTemporaryReceipt;

    /**
     * 实交报表数
     */
    private BigDecimal actualReportAmount;

    /**
     * 当日暂收款
     */
    private BigDecimal currentTemporaryReceipt;

    /**
     * 实收现金数
     */
    private BigDecimal actualCashAmount;

    /**
     * 留存数差额
     */
    private BigDecimal retainedDifference;

    /**
     * 留存现金数
     */
    private BigDecimal retainedCash;

    /**
     * 备用金
     */
    private BigDecimal pettyCash;

    // constructors, getters, setters...

    /**
     * 便捷方法：安全获取BigDecimal值
     */
    public BigDecimal getSafeValue(Function<RowData, BigDecimal> getter) {
        BigDecimal value = getter.apply(this);
        return value != null ? value : BigDecimal.ZERO;
    }
}