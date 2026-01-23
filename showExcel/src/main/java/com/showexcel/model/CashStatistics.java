package com.showexcel.model;


import java.math.BigDecimal;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2025/10/29 09:44
 */

public class CashStatistics {
    private Integer id;
    private Integer tableType;
    private String name;
    private BigDecimal hisAdvancePayment; //his
    private BigDecimal hisMedicalIncome; //his
    private BigDecimal hisRegistrationIncome; //his
    private BigDecimal reportAmount;    // 应交报表数

    private BigDecimal previousTemporaryReceipt;    // 前日暂收款

    private BigDecimal actualReportAmount;    // 实交报表数 = 应交报表数 - 前日暂收款

    private BigDecimal currentTemporaryReceipt;    // 当日暂收款

    private BigDecimal actualCashAmount;     // 实收现金数 = 实交报表数 + 当日暂收款

    private BigDecimal retainedDifference;    // 留存数差额 = 留存现金数 - 实交报表数 - 备用金

    private BigDecimal retainedCash;     // 留存现金数

    private BigDecimal pettyCash;    // 备用金

    private String remarks;     // 备注信息


    // 构造方法
    public CashStatistics() {}

    // 计算公式的方法 - 简化版，因为null值已经在setter中处理为0.0
    public void calculateFormulas() {
        // 所有字段都已经确保不为null，直接计算
        this.actualReportAmount = reportAmount.subtract(hisRegistrationIncome);
//        this.currentTemporaryReceipt = actualReportAmount.add(currentTemporaryReceipt);
        this.retainedDifference = retainedCash.subtract(pettyCash).subtract(actualReportAmount);
        this.actualCashAmount = actualReportAmount.add(currentTemporaryReceipt);
    }

    // 私有方法：统一处理BigDecimal类型的null值
    private BigDecimal safeBigDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }


    // getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getHisAdvancePayment() { return safeBigDecimal(hisAdvancePayment); }
    public void setHisAdvancePayment(BigDecimal hisAdvancePayment) { this.hisAdvancePayment = safeBigDecimal(hisAdvancePayment); }

    public BigDecimal
    getHisMedicalIncome() { return safeBigDecimal(hisMedicalIncome); }
    public void setHisMedicalIncome(BigDecimal hisMedicalIncome) { this.hisMedicalIncome = safeBigDecimal(hisMedicalIncome); }

    public BigDecimal getHisRegistrationIncome() { return safeBigDecimal(hisRegistrationIncome); }
    public void setHisRegistrationIncome(BigDecimal hisRegistrationIncome) { this.hisRegistrationIncome = safeBigDecimal(hisRegistrationIncome); }

    public BigDecimal getReportAmount() { return safeBigDecimal(reportAmount); }
    public void setReportAmount(BigDecimal reportAmount) { this.reportAmount = safeBigDecimal(reportAmount); }

    public BigDecimal getPreviousTemporaryReceipt() { return safeBigDecimal(previousTemporaryReceipt); }
    public void setPreviousTemporaryReceipt(BigDecimal previousTemporaryReceipt) { this.previousTemporaryReceipt = safeBigDecimal(previousTemporaryReceipt); }

    public BigDecimal getActualReportAmount() { return safeBigDecimal(actualReportAmount); }
    public void setActualReportAmount(BigDecimal actualReportAmount) { this.actualReportAmount = safeBigDecimal(actualReportAmount); }

    public BigDecimal getCurrentTemporaryReceipt() { return safeBigDecimal(currentTemporaryReceipt); }
    public void setCurrentTemporaryReceipt(BigDecimal currentTemporaryReceipt) { this.currentTemporaryReceipt = safeBigDecimal(currentTemporaryReceipt); }

    public BigDecimal getActualCashAmount() { return safeBigDecimal(actualCashAmount); }
    public void setActualCashAmount(BigDecimal actualCashAmount) { this.actualCashAmount = safeBigDecimal(actualCashAmount); }

    public BigDecimal getRetainedDifference() { return safeBigDecimal(retainedDifference); }
    public void setRetainedDifference(BigDecimal retainedDifference) { this.retainedDifference = safeBigDecimal(retainedDifference); }

    public BigDecimal getRetainedCash() { return safeBigDecimal(retainedCash); }
    public void setRetainedCash(BigDecimal retainedCash) { this.retainedCash = safeBigDecimal(retainedCash); }

    public BigDecimal getPettyCash() { return safeBigDecimal(pettyCash); }
    public void setPettyCash(BigDecimal pettyCash) { this.pettyCash = safeBigDecimal(pettyCash); }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Integer getTableType() {
        return tableType;
    }

    public void setTableType(Integer tableType) {
        this.tableType = tableType;
    }
}