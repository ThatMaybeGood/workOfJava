package com.mergedata.model.dto.external;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/*
 * 门诊现金报表过程接收
 */
@Data
public class HisOutpIncomeResponseDTO {
    /*
    操作员编号
     */
    @JsonProperty("operator_no")
    private String  operatorNo;

    /*
    用户名
     */
    @JsonProperty("operator_name")
    public  String operatorName;

    /*
    his预交金
     */
    @JsonProperty("his_advance_payment")
    private BigDecimal hisAdvancePayment;

    /*
    his门诊收入
     */
    @JsonProperty("his_medical_income")
    private BigDecimal  hisMedicalIncome;

    /*
    his挂号收入
     */
    @JsonProperty("his_registration_income")
    private BigDecimal  HisRegistrationIncome;

    /*
    报表日期
     */
    @JsonProperty("report_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDate reportDate;

    /*
    结账号
     */
    @JsonProperty("acct_no")
    private String acctNo;

    /*
    结账日期
     */
    @JsonProperty("acct_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime acctDate;
}