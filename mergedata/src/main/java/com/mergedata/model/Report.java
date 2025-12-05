package com.mergedata.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class Report {
    @JsonProperty("serial_no")
    private String serialNo;

    // 操作员基础信息
    @NotBlank(message = "操作员编号不能为空")
    @JsonProperty("operator_no")
    private String operatorNo;
    @JsonProperty("operator_name")
    private String operatorName;

    // HisData 相关字段
    /*
    his预交金
     */
    @NotBlank(message = "his预交金不能为空", groups = {AddGroup.class})
    @JsonProperty("his_advance_payment")
    private BigDecimal hisAdvancePayment = BigDecimal.ZERO;

    /*
    his医疗收入
     */
    @JsonProperty("his_medical_income")
    @NotBlank(message = "his医疗收入不能为空", groups = {AddGroup.class})
    private BigDecimal hisMedicalIncome = BigDecimal.ZERO;

    /*
    his挂号收入  暂时默认0
    */
    @JsonProperty("his_registration_income")
    private BigDecimal hisRegistrationIncome = BigDecimal.ZERO;

    // YQCashRegRecord 相关字段
    /*
    留存现金数
     */
    @NotBlank(message = "留存现金数不能为空", groups = {AddGroup.class} )
    @JsonProperty("retained_cash")
    private BigDecimal retainedCash = BigDecimal.ZERO;

    // 其他报表字段（根据您提供的字段）

    /*
    应交报表数
     */
    @NotBlank(message = "应交报表数不能为空", groups = {AddGroup.class})
    @JsonProperty("report_amount")
    private BigDecimal reportAmount = BigDecimal.ZERO;

    /*
    前日暂收款 正常情况为前一天的当日暂收款
    */
    @JsonProperty("previous_temporary_receipt")
    private BigDecimal previousTemporaryReceipt = BigDecimal.ZERO; // 默认0

    /*
     节假日暂收款
     */
    @JsonProperty("holiday_temporary_receipt")
    private BigDecimal holidayTemporaryReceipt = BigDecimal.ZERO;  // 默认0

    /*
    实交报表数
     */
    @NotBlank(message = "实交报表数不能为空", groups = {AddGroup.class})
    @JsonProperty("actual_report_amount")
    private BigDecimal actualReportAmount = BigDecimal.ZERO;


    // YQCashRegRecord 相关字段

    /*
    当日暂收款
     */
    @NotBlank(message = "当日暂收款不能为空", groups = {AddGroup.class})
    @JsonProperty("current_temporary_receipt")
    private BigDecimal currentTemporaryReceipt = BigDecimal.ZERO;  // 默认0

    /*
    实收现金数
     */
    @NotBlank(message = "实收现金数不能为空", groups = {AddGroup.class})
    @JsonProperty("actual_cash_amount")
    private BigDecimal actualCashAmount = BigDecimal.ZERO;         // 默认0

    /*
    留存差额数
     */
    @NotBlank(message = "留存差额数不能为空", groups = {AddGroup.class})
    @JsonProperty("retained_difference")
    private BigDecimal retainedDifference = BigDecimal.ZERO;       // 默认0

    /*
    备用金
     */
    @NotBlank(message = "备用金不能为空", groups = {AddGroup.class})
    @JsonProperty("petty_cash")
    private BigDecimal pettyCash = BigDecimal.ZERO;                // 默认0

    /*
    备注信息
     */
    @JsonProperty("remarks")
    private String remarks;



    /*
    报表日期
     */
    @NotBlank(message = "报表日期不能为空", groups = {AddGroup.class})
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @JsonProperty("report_date")
    private LocalDate reportDate;

    /*
    报表年份
     */
    @JsonProperty("report_year")
    private String reportYear;

    /*
    报表创建时间
     */
//    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8") // <-- 新增此行
    @JsonProperty("create_time")
    private LocalDate createTime;


}
