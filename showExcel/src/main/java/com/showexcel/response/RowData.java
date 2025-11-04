package com.showexcel.response;

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
    private BigDecimal actualReportAmount;  //实交报表数 = 应交报表数 - 前日暂收款

    /**
     * 当日暂收款
     */
    private BigDecimal currentTemporaryReceipt;

    /**
     * 实收现金数
     */
    private BigDecimal actualCashAmount;  // 实收现金数 = 实交报表数 + 当日暂收款

    /**
     * 留存数差额
     */
    private BigDecimal retainedDifference;  // 留存数差额 = 留存现金数 - 实交报表数 - 备用金

    /**
     * 留存现金数
     */
    private BigDecimal retainedCash;

    /**
     * 备用金
     */
    private BigDecimal pettyCash;

    /**
     * 备注信息
     */
    private String remarks;

    // constructors, getters, setters...

    /**
     * 便捷方法：安全获取BigDecimal值
     */
    public BigDecimal safeBigDecimal(BigDecimal value) {
        return value != null ? value : new BigDecimal("0.00");
    }

    /**
     * 为所有BigDecimal字段提供默认值设置
     */

    public BigDecimal getHisAdvancePayment() {
        return safeBigDecimal(this.hisAdvancePayment);
    }

    public BigDecimal getHisMedicalIncome() {
        return safeBigDecimal(this.hisMedicalIncome);
    }

    public BigDecimal getHisRegistrationIncome() {
        return safeBigDecimal(this.hisRegistrationIncome);
    }

    public BigDecimal getReportAmount() {
        return safeBigDecimal(this.reportAmount);
    }

    public BigDecimal getPreviousTemporaryReceipt() {
        return safeBigDecimal(this.previousTemporaryReceipt);
    }


    public BigDecimal getCurrentTemporaryReceipt() {
        return safeBigDecimal(this.currentTemporaryReceipt);
    }


    public BigDecimal getRetainedCash() {
        return safeBigDecimal(this.retainedCash);
    }

    public BigDecimal getPettyCash() {
        return safeBigDecimal(this.pettyCash);
    }

}