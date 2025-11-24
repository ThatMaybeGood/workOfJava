package com.example.messagedataservice.dto;

import java.util.Date;

public class ReportDTO {
    // 操作员基础信息
    private String operatorNo;
    private String operatorName;

    // HisData 相关字段
    /*
    his预交金
     */
    private Double hisAdvancePayment = 0.00;

    /*
    his医疗收入
     */
    private Double hisMedicalIncome = 0.00;

    /*
    his挂号收入  暂时默认0
    */
    private Double hisRegistrationIncome = 0.00;


    // YQCashRegRecord 相关字段
    /*
    留存现金数
     */
    private Double retainedCash = 0.00;
    private String windowNo;
    private String operatType;
    private String sechduling;
    private Date applyDate;


    // 其他报表字段（根据您提供的字段）

    /*
    应交报表数
     */
    private Double reportAmount = 0.00;

    /*
    前日暂收款 正常情况为前一天的当日暂收款
    */
    private Double previousTemporaryReceipt = 0.00; // 默认0

    /*
     节假日暂收款
     */
    private Double holidayTemporaryReceipt = 0.00;  // 默认0

    /*
    实交报表数
     */
    private Double actualReportAmount = 0.00;       // 默认0

    /*
    当日暂收款
     */
    private Double currentTemporaryReceipt = 0.00;  // 默认0

    /*
    实收现金数
     */
    private Double actualCashAmount = 0.00;         // 默认0

    /*
    留存差额数
     */
    private Double retainedDifference = 0.00;       // 默认0

    /*
    备用金
     */
    private Double pettyCash = 0.00;                // 默认0

    /*
    备注信息
     */
    private String remarks;


    private Date reportDate;
    private Date createTime;

    public String getOperatorNo() {
        return operatorNo;
    }

    public void setOperatorNo(String operatorNo) {
        this.operatorNo = operatorNo;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public Double getHisAdvancePayment() {
        return hisAdvancePayment;
    }

    public void setHisAdvancePayment(Double hisAdvancePayment) {
        this.hisAdvancePayment = hisAdvancePayment;
    }

    public Double getHisMedicalIncome() {
        return hisMedicalIncome;
    }

    public void setHisMedicalIncome(Double hisMedicalIncome) {
        this.hisMedicalIncome = hisMedicalIncome;
    }

    public Double getRetainedCash() {
        return retainedCash;
    }

    public void setRetainedCash(Double retainedCash) {
        this.retainedCash = retainedCash;
    }

    public String getWindowNo() {
        return windowNo;
    }

    public void setWindowNo(String windowNo) {
        this.windowNo = windowNo;
    }

    public String getOperatType() {
        return operatType;
    }

    public void setOperatType(String operatType) {
        this.operatType = operatType;
    }

    public String getSechduling() {
        return sechduling;
    }

    public void setSechduling(String sechduling) {
        this.sechduling = sechduling;
    }

    public Date getApplyDate() {
        return applyDate;
    }

    public void setApplyDate(Date applyDate) {
        this.applyDate = applyDate;
    }

    public Double getHisRegistrationIncome() {
        return hisRegistrationIncome;
    }

    public void setHisRegistrationIncome(Double hisRegistrationIncome) {
        this.hisRegistrationIncome = hisRegistrationIncome;
    }

    public Double getReportAmount() {
        return reportAmount;
    }

    public void setReportAmount(Double reportAmount) {
        this.reportAmount = reportAmount;
    }

    public Double getPreviousTemporaryReceipt() {
        return previousTemporaryReceipt;
    }

    public void setPreviousTemporaryReceipt(Double previousTemporaryReceipt) {
        this.previousTemporaryReceipt = previousTemporaryReceipt;
    }

    public Double getHolidayTemporaryReceipt() {
        return holidayTemporaryReceipt;
    }

    public void setHolidayTemporaryReceipt(Double holidayTemporaryReceipt) {
        this.holidayTemporaryReceipt = holidayTemporaryReceipt;
    }

    public Double getActualReportAmount() {
        return actualReportAmount;
    }

    public void setActualReportAmount(Double actualReportAmount) {
        this.actualReportAmount = actualReportAmount;
    }

    public Double getCurrentTemporaryReceipt() {
        return currentTemporaryReceipt;
    }

    public void setCurrentTemporaryReceipt(Double currentTemporaryReceipt) {
        this.currentTemporaryReceipt = currentTemporaryReceipt;
    }

    public Double getActualCashAmount() {
        return actualCashAmount;
    }

    public void setActualCashAmount(Double actualCashAmount) {
        this.actualCashAmount = actualCashAmount;
    }

    public Double getRetainedDifference() {
        return retainedDifference;
    }

    public void setRetainedDifference(Double retainedDifference) {
        this.retainedDifference = retainedDifference;
    }

    public Double getPettyCash() {
        return pettyCash;
    }

    public void setPettyCash(Double pettyCash) {
        this.pettyCash = pettyCash;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
