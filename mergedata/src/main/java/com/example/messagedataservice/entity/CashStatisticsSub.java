package com.example.messagedataservice.entity;

public class CashStatisticsSub {
    // 关联的主表流水号
    private String serialNo;
    private String hisOperatorNo;
    private String hisOperatorName;

    private Double hisAdvancePayment; //his
    private Double hisMedicalIncome; //his
    private Double hisRegistrationIncome; //his


    private Double reportAmount;    // LocalDate

    private Double previousTemporaryReceipt;    // 前日暂收款

    private Double holidayTemporaryReceipt;  //节假日暂收款

    private Double actualReportAmount;    // 实交报表数 = LocalDate - 前日暂收款

    private Double currentTemporaryReceipt;    // 当日暂收款

    private Double actualCashAmount;     // 实收现金数 = 实交报表数 + 当日暂收款

    private Double retainedDifference;    // 留存数差额 = 留存现金数 - 实交报表数 - 备用金

    private Double retainedCash;     // 留存现金数

    private Double pettyCash;    // 备用金

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

    // 私有方法：统一处理Double类型的null值
    private Double safeDouble(Double value) {
        return value != null ? value : 0.00;
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

    public Double getHisAdvancePayment() {
        return safeDouble(hisAdvancePayment);
    }

    public void setHisAdvancePayment(Double hisAdvancePayment) {
        this.hisAdvancePayment = hisAdvancePayment;
    }

    public Double getHisMedicalIncome() {
        return safeDouble(hisMedicalIncome);
    }

    public void setHisMedicalIncome(Double hisMedicalIncome) {
        this.hisMedicalIncome = hisMedicalIncome;
    }

    public Double getHisRegistrationIncome() {
        return safeDouble(hisRegistrationIncome);
    }

    public void setHisRegistrationIncome(Double hisRegistrationIncome) {
        this.hisRegistrationIncome = hisRegistrationIncome;
    }

    public Double getReportAmount() {
        return safeDouble(reportAmount);
    }

    public void setReportAmount(Double reportAmount) {
        this.reportAmount = reportAmount;
    }

    public Double getPreviousTemporaryReceipt() {
        return safeDouble(previousTemporaryReceipt);
    }

    public void setPreviousTemporaryReceipt(Double previousTemporaryReceipt) {
        this.previousTemporaryReceipt = previousTemporaryReceipt;
    }

    public Double getHolidayTemporaryReceipt() {
        return safeDouble(holidayTemporaryReceipt);
    }

    public void setHolidayTemporaryReceipt(Double holidayTemporaryReceipt) {
        this.holidayTemporaryReceipt = holidayTemporaryReceipt;
    }

    public Double getActualReportAmount() {
        return safeDouble(actualReportAmount);
    }

    public void setActualReportAmount(Double actualReportAmount) {
        this.actualReportAmount = actualReportAmount;
    }

    public Double getCurrentTemporaryReceipt() {
        return safeDouble(currentTemporaryReceipt);
    }

    public void setCurrentTemporaryReceipt(Double currentTemporaryReceipt) {
        this.currentTemporaryReceipt = currentTemporaryReceipt;
    }

    public Double getActualCashAmount() {
        return safeDouble(actualCashAmount);
    }

    public void setActualCashAmount(Double actualCashAmount) {
        this.actualCashAmount = actualCashAmount;
    }

    public Double getRetainedDifference() {
        return safeDouble(retainedDifference);
    }

    public void setRetainedDifference(Double retainedDifference) {
        this.retainedDifference = retainedDifference;
    }

    public Double getRetainedCash() {
        return safeDouble(retainedCash);
    }

    public void setRetainedCash(Double retainedCash) {
        this.retainedCash = retainedCash;
    }

    public Double getPettyCash() {
        return safeDouble(pettyCash);
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
}
