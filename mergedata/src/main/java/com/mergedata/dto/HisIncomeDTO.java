package com.mergedata.dto;// File: com.mergedata.dto.HisIncomeDTO.java (您需要修改或确认该文件)

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class HisIncomeDTO {

    @JsonProperty("operator_no")
    private String  operatorNo;

    @JsonProperty("his_advance_payment") // <-- 确保有此注解
    private BigDecimal hisAdvancePayment;

    // 注意：字段名称在 JSON 中是下划线 his_medical_income，但 DTO 中用驼峰
    // 因为 JacksonConfig 中配置了 SNAKE_CASE，所以这里继续使用驼峰命名
    @JsonProperty("his_medical_income") // <-- 确保有此注解
    private BigDecimal  hisMedicalIncome;

    @JsonProperty("report_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reportDate;

    // ... 其他字段也应如此
}