package com.example.messagedataservice.model;

import java.util.Date;

public class HisData {
    private String  operatorNo;
    private Double  hisAdvancePayment; //his
    private Double  hisMedicalIncome; //his
    private Date  reportDate;

    public String getOperatorNo() {
        return operatorNo;
    }

    public void setOperatorNo(String operatorNo) {
        this.operatorNo = operatorNo;
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

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }
}
