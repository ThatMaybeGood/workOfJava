package com.mergedata.model;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Oracle存储过程参数实体类 - 使用JsonProperty注解
 */
@Data
public class ReportInsert {

    @JsonProperty("A_SERIAL_NO")
    private String A_SERIAL_NO;

    @JsonProperty("A_REPORT_DATE")
    private String A_REPORT_DATE;

    @JsonProperty("A_REPORT_YEAR")
    private String A_REPORT_YEAR;

    @JsonProperty("A_EMP_ID")
    private String A_EMP_ID;

    @JsonProperty("A_EMP_NAME")
    private String A_EMP_NAME;

    @JsonProperty("A_HISADVANCEPAYMENT")
    private BigDecimal A_HISADVANCEPAYMENT;

    @JsonProperty("A_HISMEDICALINCOME")
    private BigDecimal A_HISMEDICALINCOME;

    @JsonProperty("A_HISREGISTRATIONINCOME")
    private BigDecimal A_HISREGISTRATIONINCOME;

    @JsonProperty("A_REPORTAMOUNT")
    private BigDecimal A_REPORTAMOUNT;

    @JsonProperty("A_PREVIOUSTEMPORARYRECEIPT")
    private BigDecimal A_PREVIOUSTEMPORARYRECEIPT;

    @JsonProperty("A_HOLIDAYTEMPORARYRECEIPT")
    private BigDecimal A_HOLIDAYTEMPORARYRECEIPT;

    @JsonProperty("A_ACTUALREPORTAMOUNT")
    private BigDecimal A_ACTUALREPORTAMOUNT;

    @JsonProperty("A_CURRENTTEMPORARYRECEIPT")
    private BigDecimal A_CURRENTTEMPORARYRECEIPT;

    @JsonProperty("A_ACTUALCASHAMOUNT")
    private BigDecimal A_ACTUALCASHAMOUNT;

    @JsonProperty("A_RETAINEDDIFFERENCE")
    private BigDecimal A_RETAINEDDIFFERENCE;

    @JsonProperty("A_RETAINEDCASH")
    private BigDecimal A_RETAINEDCASH;

    @JsonProperty("A_PETTYCASH")
    private BigDecimal A_PETTYCASH;

    @JsonProperty("A_REMARKS")
    private String A_REMARKS;

    @JsonProperty("A_TYPE")
    private String A_TYPE;

    @JsonProperty("A_ISINSERTMASTER")
    private String A_ISINSERTMASTER;

    @JsonProperty("A_RETCODE")
    private Integer A_RETCODE;

    @JsonProperty("A_ERRMSG")
    private String ERRMSG;
}