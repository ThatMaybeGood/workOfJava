package com.showexcel.dto;

import com.showexcel.model.CashStatistics;

public class CashStatisticsDTO {
    private Integer id;
    private Integer type;
    private String name;
    private Double hisAdvancePayment;
    private Double hisMedicalIncome;
    private Double hisRegistrationIncome;
    private Double reportAmount;
    private Double previousTemporaryReceipt;
    private Double actualReportAmount;
    private Double currentTemporaryReceipt;
    private Double actualCashAmount;
    private Double retainedDifference;
    private Double retainedCash;
    private Double pettyCash;
    private String remarks;

    // 合并单元格相关属性
    private Integer rowspan = 1;
    private Integer colspan = 1;
    private Boolean isMerged = false;
    private String displayName;

    // 构造方法
    public CashStatisticsDTO() {}

    public CashStatisticsDTO(CashStatistics entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.hisAdvancePayment = entity.getHisAdvancePayment();
        this.hisMedicalIncome = entity.getHisMedicalIncome();
        this.hisRegistrationIncome = entity.getHisRegistrationIncome();
        this.reportAmount = entity.getReportAmount();
        this.previousTemporaryReceipt = entity.getPreviousTemporaryReceipt();
        this.actualReportAmount = entity.getActualReportAmount();
        this.currentTemporaryReceipt = entity.getCurrentTemporaryReceipt();
        this.actualCashAmount = entity.getActualCashAmount();
        this.retainedDifference = entity.getRetainedDifference();
        this.retainedCash = entity.getRetainedCash();
        this.pettyCash = entity.getPettyCash();
        this.remarks = entity.getRemarks();
        this.displayName = entity.getName();
    }

    // getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Double getCurrentTemporaryReceipt() {
        return currentTemporaryReceipt;
    }

    public void setCurrentTemporaryReceipt(Double currentTemporaryReceipt) {
        this.currentTemporaryReceipt = currentTemporaryReceipt;
    }

    public Boolean getMerged() {
        return isMerged;
    }

    public void setMerged(Boolean merged) {
        isMerged = merged;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getHisAdvancePayment() { return hisAdvancePayment; }
    public void setHisAdvancePayment(Double hisAdvancePayment) { this.hisAdvancePayment = hisAdvancePayment; }

    public Double getHisMedicalIncome() { return hisMedicalIncome; }
    public void setHisMedicalIncome(Double hisMedicalIncome) { this.hisMedicalIncome = hisMedicalIncome; }

    public Double getHisRegistrationIncome() { return hisRegistrationIncome; }
    public void setHisRegistrationIncome(Double hisRegistrationIncome) { this.hisRegistrationIncome = hisRegistrationIncome; }

    public Double getReportAmount() { return reportAmount; }
    public void setReportAmount(Double reportAmount) { this.reportAmount = reportAmount; }

    public Double getPreviousTemporaryReceipt() { return previousTemporaryReceipt; }
    public void setPreviousTemporaryReceipt(Double previousTemporaryReceipt) { this.previousTemporaryReceipt = previousTemporaryReceipt; }

    public Double getActualReportAmount() { return actualReportAmount; }
    public void setActualReportAmount(Double actualReportAmount) { this.actualReportAmount = actualReportAmount; }

    public Double getActualCashAmount() { return actualCashAmount; }
    public void setActualCashAmount(Double actualCashAmount) { this.actualCashAmount = actualCashAmount; }

    public Double getRetainedDifference() { return retainedDifference; }
    public void setRetainedDifference(Double retainedDifference) { this.retainedDifference = retainedDifference; }

    public Double getRetainedCash() { return retainedCash; }
    public void setRetainedCash(Double retainedCash) { this.retainedCash = retainedCash; }

    public Double getPettyCash() { return pettyCash; }
    public void setPettyCash(Double pettyCash) { this.pettyCash = pettyCash; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public Integer getRowspan() { return rowspan; }
    public void setRowspan(Integer rowspan) { this.rowspan = rowspan; }

    public Integer getColspan() { return colspan; }
    public void setColspan(Integer colspan) { this.colspan = colspan; }

    public Boolean getIsMerged() { return isMerged; }
    public void setIsMerged(Boolean isMerged) { this.isMerged = isMerged; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}