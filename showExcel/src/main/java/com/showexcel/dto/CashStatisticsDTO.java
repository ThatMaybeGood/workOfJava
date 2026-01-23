package com.showexcel.dto;

import com.showexcel.model.CashStatistics;

import java.math.BigDecimal;

public class CashStatisticsDTO {
    private Integer id;
    private Integer type;
    private String name;
    private BigDecimal hisAdvancePayment;
    private BigDecimal hisMedicalIncome;
    private BigDecimal hisRegistrationIncome;
    private BigDecimal reportAmount;
    private BigDecimal previousTemporaryReceipt;
    private BigDecimal actualReportAmount;
    private BigDecimal currentTemporaryReceipt;
    private BigDecimal actualCashAmount;
    private BigDecimal retainedDifference;
    private BigDecimal retainedCash;
    private BigDecimal pettyCash;
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

    public BigDecimal getCurrentTemporaryReceipt() {
        return currentTemporaryReceipt;
    }

    public void setCurrentTemporaryReceipt(BigDecimal currentTemporaryReceipt) {
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

    public BigDecimal getHisAdvancePayment() { return hisAdvancePayment; }
    public void setHisAdvancePayment(BigDecimal hisAdvancePayment) { this.hisAdvancePayment = hisAdvancePayment; }

    public BigDecimal getHisMedicalIncome() { return hisMedicalIncome; }
    public void setHisMedicalIncome(BigDecimal hisMedicalIncome) { this.hisMedicalIncome = hisMedicalIncome; }

    public BigDecimal getHisRegistrationIncome() { return hisRegistrationIncome; }
    public void setHisRegistrationIncome(BigDecimal hisRegistrationIncome) { this.hisRegistrationIncome = hisRegistrationIncome; }

    public BigDecimal getReportAmount() { return reportAmount; }
    public void setReportAmount(BigDecimal reportAmount) { this.reportAmount = reportAmount; }

    public BigDecimal getPreviousTemporaryReceipt() { return previousTemporaryReceipt; }
    public void setPreviousTemporaryReceipt(BigDecimal previousTemporaryReceipt) { this.previousTemporaryReceipt = previousTemporaryReceipt; }

    public BigDecimal getActualReportAmount() { return actualReportAmount; }
    public void setActualReportAmount(BigDecimal actualReportAmount) { this.actualReportAmount = actualReportAmount; }

    public BigDecimal getActualCashAmount() { return actualCashAmount; }
    public void setActualCashAmount(BigDecimal actualCashAmount) { this.actualCashAmount = actualCashAmount; }

    public BigDecimal getRetainedDifference() { return retainedDifference; }
    public void setRetainedDifference(BigDecimal retainedDifference) { this.retainedDifference = retainedDifference; }

    public BigDecimal getRetainedCash() { return retainedCash; }
    public void setRetainedCash(BigDecimal retainedCash) { this.retainedCash = retainedCash; }

    public BigDecimal getPettyCash() { return pettyCash; }
    public void setPettyCash(BigDecimal pettyCash) { this.pettyCash = pettyCash; }

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