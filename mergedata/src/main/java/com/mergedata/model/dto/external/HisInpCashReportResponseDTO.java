package com.mergedata.model.dto.external;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/*
 * 住院现金报表接收
 */
@Data
public class HisInpCashReportResponseDTO {
    /*
    操作员编号
     */
    @JsonProperty("user_id")
    private String  operatorNo;
    /*
    用户名
     */
    @JsonProperty("user_name")
    public  String operatorName;

    /*
    his预交金
     */
    @JsonProperty("advance_payment") //
    private BigDecimal hisAdvancePayment;

    /*
    his结算收入
     */
    @JsonProperty("settlement_income") //
    private BigDecimal  hisSettlementIncome;

    /*
    his院前收入
     */
    @JsonProperty("pre_hospital_income")
    private BigDecimal  hisPreHospitalIncome;

    /*
    报表日期
     */
    @JsonProperty("report_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDate reportDate;

}