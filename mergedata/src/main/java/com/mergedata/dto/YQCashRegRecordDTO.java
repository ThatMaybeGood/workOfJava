package com.mergedata.dto;


import java.time.LocalDate;

// 定义一个类，用于存储医院现金报表记录
public class YQCashRegRecordDTO {
    private String  operatorNo;
    //留存现金数
    private Double  retainedCash; //his

    private LocalDate  applyDate;

    private LocalDate createTime;

    private String operatorName;

    public String operatType;

    public String windowNo;

    public String sechduling;

    //报表日期
    public String saveDate;


    public String getOperatorNo() {
        return operatorNo;
    }

    public void setOperatorNo(String operatorNo) {
        this.operatorNo = operatorNo;
    }

    public Double getRetainedCash() {
        return retainedCash;
    }

    public void setRetainedCash(Double retainedCash) {
        this.retainedCash = retainedCash;
    }

    public LocalDate getApplyDate() {
        return applyDate;
    }

    public void setApplyDate(LocalDate applyDate) {
        this.applyDate = applyDate;
    }

    public LocalDate getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDate createTime) {
        this.createTime = createTime;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getOperatType() {
        return operatType;
    }

    public void setOperatType(String operatType) {
        this.operatType = operatType;
    }

    public String getWindowNo() {
        return windowNo;
    }

    public void setWindowNo(String windowNo) {
        this.windowNo = windowNo;
    }

    public String getSechduling() {
        return sechduling;
    }

    public void setSechduling(String sechduling) {
        this.sechduling = sechduling;
    }

    public String getSaveDate() {
        return saveDate;
    }

    public void setSaveDate(String saveDate) {
        this.saveDate = saveDate;
    }
}
