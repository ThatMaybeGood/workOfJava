package com.mergedata.model.dto.external;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class HisIncomeResponseDTO {

    @JsonProperty("operator_no")
    private String  operatorNo;

    @JsonProperty("operator_name")
    public  String operatorName;

    @JsonProperty("his_advance_payment") //
    private BigDecimal hisAdvancePayment;

    // 注意：字段名称在 JSON 中是下划线 his_medical_income，但 DTO 中用驼峰
    // 因为 JacksonConfig 中配置了 SNAKE_CASE，所以这里继续使用驼峰命名
    @JsonProperty("his_medical_income") //
    private BigDecimal  hisMedicalIncome;

    @JsonProperty("his_registration_income") //
    private BigDecimal  HisRegistrationIncome;


    @JsonProperty("report_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDate reportDate;

    // ... 其他字段也应如此

    @JsonProperty("acct_no")
    private String acctNo;

    @JsonProperty("acct_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime acctDate;
}