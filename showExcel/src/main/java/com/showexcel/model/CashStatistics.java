package com.showexcel.model;

import java.util.Map;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2025/10/29 09:44
 */

public class CashStatistics {
    private Integer id;
    private Integer type;
    private String name;
    private Double hisAdvancePayment; //his
    private Double hisMedicalIncome; //his
    private Double hisRegistrationIncome; //his
    private Double reportAmount;    // 应交报表数

    private Double previousTemporaryReceipt;    // 前日暂收款

    private Double actualReportAmount;    // 实交报表数 = 应交报表数 - 前日暂收款

    private Double currentTemporaryReceipt;    // 当日暂收款

    private Double actualCashAmount;     // 实收现金数 = 实交报表数 + 当日暂收款

    private Double retainedDifference;    // 留存数差额 = 留存现金数 - 实交报表数 - 备用金

    private Double retainedCash;     // 留存现金数

    private Double pettyCash;    // 备用金

    private String remarks;     // 备注信息

    private Map<String, Object> mergeInfo; // 用于Excel单元格合并信息


    // 构造方法
    public CashStatistics() {}

    // 计算公式的方法 - 简化版，因为null值已经在setter中处理为0.0
    public void calculateFormulas() {
        // 所有字段都已经确保不为null，直接计算
        this.actualReportAmount = reportAmount - hisRegistrationIncome;
        this.currentTemporaryReceipt = actualReportAmount + currentTemporaryReceipt;
        this.retainedDifference = retainedCash - pettyCash - actualReportAmount;
        this.actualCashAmount = actualReportAmount + currentTemporaryReceipt;
    }

    // 私有方法：统一处理Double类型的null值
    private Double safeDouble(Double value) {
        return value != null ? value : 0.00;
    }


    // getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getHisAdvancePayment() { return safeDouble(hisAdvancePayment); }
    public void setHisAdvancePayment(Double hisAdvancePayment) { this.hisAdvancePayment = safeDouble(hisAdvancePayment); }

    public Double
    getHisMedicalIncome() { return safeDouble(hisMedicalIncome); }
    public void setHisMedicalIncome(Double hisMedicalIncome) { this.hisMedicalIncome = safeDouble(hisMedicalIncome); }

    public Double getHisRegistrationIncome() { return safeDouble(hisRegistrationIncome); }
    public void setHisRegistrationIncome(Double hisRegistrationIncome) { this.hisRegistrationIncome = safeDouble(hisRegistrationIncome); }

    public Double getReportAmount() { return safeDouble(reportAmount); }
    public void setReportAmount(Double reportAmount) { this.reportAmount = safeDouble(reportAmount); }

    public Double getPreviousTemporaryReceipt() { return safeDouble(previousTemporaryReceipt); }
    public void setPreviousTemporaryReceipt(Double previousTemporaryReceipt) { this.previousTemporaryReceipt = safeDouble(previousTemporaryReceipt); }

    public Double getActualReportAmount() { return safeDouble(actualReportAmount); }
    public void setActualReportAmount(Double actualReportAmount) { this.actualReportAmount = safeDouble(actualReportAmount); }

    public Double getCurrentTemporaryReceipt() { return safeDouble(currentTemporaryReceipt); }
    public void setCurrentTemporaryReceipt(Double currentTemporaryReceipt) { this.currentTemporaryReceipt = safeDouble(currentTemporaryReceipt); }

    public Double getActualCashAmount() { return safeDouble(actualCashAmount); }
    public void setActualCashAmount(Double actualCashAmount) { this.actualCashAmount = safeDouble(actualCashAmount); }

    public Double getRetainedDifference() { return safeDouble(retainedDifference); }
    public void setRetainedDifference(Double retainedDifference) { this.retainedDifference = safeDouble(retainedDifference); }

    public Double getRetainedCash() { return safeDouble(retainedCash); }
    public void setRetainedCash(Double retainedCash) { this.retainedCash = safeDouble(retainedCash); }

    public Double getPettyCash() { return safeDouble(pettyCash); }
    public void setPettyCash(Double pettyCash) { this.pettyCash = safeDouble(pettyCash); }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Map<String, Object> getMergeInfo() {
        return mergeInfo;
    }

    public void setMergeInfo(Map<String, Object> mergeInfo) {
        this.mergeInfo = mergeInfo;
    }

}