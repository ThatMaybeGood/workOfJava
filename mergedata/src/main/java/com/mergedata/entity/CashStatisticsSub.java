package com.mergedata.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CashStatisticsSub {
    // 关联的主表流水号
    private String serialNo;
    private String hisOperatorNo;
    private String hisOperatorName;

    private BigDecimal hisAdvancePayment; //his
    private BigDecimal hisMedicalIncome; //his
    private BigDecimal hisRegistrationIncome; //his


    private BigDecimal reportAmount;    // LocalDate

    private BigDecimal previousTemporaryReceipt;    // 前日暂收款

    private BigDecimal holidayTemporaryReceipt;  //节假日暂收款

    private BigDecimal actualReportAmount;    // 实交报表数 = LocalDate - 前日暂收款

    private BigDecimal currentTemporaryReceipt;    // 当日暂收款

    private BigDecimal actualCashAmount;     // 实收现金数 = 实交报表数 + 当日暂收款

    private BigDecimal retainedDifference;    // 留存数差额 = 留存现金数 - 实交报表数 - 备用金

    private BigDecimal retainedCash;     // 留存现金数

    private BigDecimal pettyCash;    // 备用金

    private String remarks;     // 备注信息


    // 构造方法
    public CashStatisticsSub() {}

//    // 计算公式的方法 - 简化版，因为null值已经在setter中处理为0.0
//    public void calculateFormulas() {
//        // 所有字段都已经确保不为null，直接计算
//        this.actualReportAmount = reportAmount - hisRegistrationIncome;
////        this.currentTemporaryReceipt = actualReportAmount + currentTemporaryReceipt;
//        this.retainedDifference = retainedCash - pettyCash - actualReportAmount;
//        this.actualCashAmount = actualReportAmount + currentTemporaryReceipt;
//    }

    // 私有方法：统一处理BigDecimal类型的null值
    private BigDecimal safeBigDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }


    // getters and setters

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getHisOperatorNo() {
        return hisOperatorNo;
    }

    public void setHisOperatorNo(String hisOperatorNo) {
        this.hisOperatorNo = hisOperatorNo;
    }

    public String getHisOperatorName() {
        return hisOperatorName;
    }

    public void setHisOperatorName(String hisOperatorName) {
        this.hisOperatorName = hisOperatorName;
    }

    public BigDecimal getHisAdvancePayment() {
        return safeBigDecimal(hisAdvancePayment);
    }

    public void setHisAdvancePayment(BigDecimal hisAdvancePayment) {
        this.hisAdvancePayment = hisAdvancePayment;
    }

    public BigDecimal getHisMedicalIncome() {
        return safeBigDecimal(hisMedicalIncome);
    }

    public void setHisMedicalIncome(BigDecimal hisMedicalIncome) {
        this.hisMedicalIncome = hisMedicalIncome;
    }

    public BigDecimal getHisRegistrationIncome() {
        return safeBigDecimal(hisRegistrationIncome);
    }

    public void setHisRegistrationIncome(BigDecimal hisRegistrationIncome) {
        this.hisRegistrationIncome = hisRegistrationIncome;
    }

    public BigDecimal getReportAmount() {
        return safeBigDecimal(reportAmount);
    }

    public void setReportAmount(BigDecimal reportAmount) {
        this.reportAmount = reportAmount;
    }

    public BigDecimal getPreviousTemporaryReceipt() {
        return safeBigDecimal(previousTemporaryReceipt);
    }

    public void setPreviousTemporaryReceipt(BigDecimal previousTemporaryReceipt) {
        this.previousTemporaryReceipt = previousTemporaryReceipt;
    }

    public BigDecimal getHolidayTemporaryReceipt() {
        return safeBigDecimal(holidayTemporaryReceipt);
    }

    public void setHolidayTemporaryReceipt(BigDecimal holidayTemporaryReceipt) {
        this.holidayTemporaryReceipt = holidayTemporaryReceipt;
    }

    public BigDecimal getActualReportAmount() {
        return safeBigDecimal(actualReportAmount);
    }

    public void setActualReportAmount(BigDecimal actualReportAmount) {
        this.actualReportAmount = actualReportAmount;
    }

    public BigDecimal getCurrentTemporaryReceipt() {
        return safeBigDecimal(currentTemporaryReceipt);
    }

    public void setCurrentTemporaryReceipt(BigDecimal currentTemporaryReceipt) {
        this.currentTemporaryReceipt = currentTemporaryReceipt;
    }

    public BigDecimal getActualCashAmount() {
        return safeBigDecimal(actualCashAmount);
    }

    public void setActualCashAmount(BigDecimal actualCashAmount) {
        this.actualCashAmount = actualCashAmount;
    }

    public BigDecimal getRetainedDifference() {
        return safeBigDecimal(retainedDifference);
    }

    public void setRetainedDifference(BigDecimal retainedDifference) {
        this.retainedDifference = retainedDifference;
    }

    public BigDecimal getRetainedCash() {
        return safeBigDecimal(retainedCash);
    }

    public void setRetainedCash(BigDecimal retainedCash) {
        this.retainedCash = retainedCash;
    }

    public BigDecimal getPettyCash() {
        return safeBigDecimal(pettyCash);
    }

    public void setPettyCash(BigDecimal pettyCash) {
        this.pettyCash = pettyCash;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
