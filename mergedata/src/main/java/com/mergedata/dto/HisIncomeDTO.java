package com.mergedata.dto;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.LocalDate;

// 核心：启用 SnakeCase 策略，将 内部字段从驼峰转为下划线
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class HisIncomeDTO {
    private String  operatorNo;
    private Double  hisAdvancePayment; //his
    private Double  hisMedicalIncome; //his
    private LocalDate  reportDate;

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

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }
}
