package com.mergedata.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class Report {

    private String serialNo;

    // 操作员基础信息
    @NotBlank(message = "操作员编号不能为空")
    private String operatorNo;
    private String operatorName;

    // HisData 相关字段
    /*
    his预交金
     */
    @NotBlank(message = "his预交金不能为空", groups = {AddGroup.class})
    private BigDecimal hisAdvancePayment = BigDecimal.ZERO;

    /*
    his医疗收入
     */
    @NotBlank(message = "his医疗收入不能为空", groups = {AddGroup.class})
    private BigDecimal hisMedicalIncome = BigDecimal.ZERO;

    /*
    his挂号收入  暂时默认0
    */
    private BigDecimal hisRegistrationIncome = BigDecimal.ZERO;


    // YQCashRegRecord 相关字段
    /*
    留存现金数
     */
    @NotBlank(message = "留存现金数不能为空", groups = {AddGroup.class} )
    private BigDecimal retainedCash = BigDecimal.ZERO;

    // 其他报表字段（根据您提供的字段）

    /*
    应交报表数
     */
    @NotBlank(message = "应交报表数不能为空", groups = {AddGroup.class})
    private BigDecimal reportAmount = BigDecimal.ZERO;

    /*
    前日暂收款 正常情况为前一天的当日暂收款
    */
    private BigDecimal previousTemporaryReceipt = BigDecimal.ZERO; // 默认0

    /*
     节假日暂收款
     */
    private BigDecimal holidayTemporaryReceipt = BigDecimal.ZERO;  // 默认0

    /*
    实交报表数
     */
    @NotBlank(message = "实交报表数不能为空", groups = {AddGroup.class})
    private BigDecimal actualReportAmount = BigDecimal.ZERO;       // 默认0

    /*
    当日暂收款
     */
    @NotBlank(message = "当日暂收款不能为空", groups = {AddGroup.class})
    private BigDecimal currentTemporaryReceipt = BigDecimal.ZERO;  // 默认0

    /*
    实收现金数
     */
    @NotBlank(message = "实收现金数不能为空", groups = {AddGroup.class})
    private BigDecimal actualCashAmount = BigDecimal.ZERO;         // 默认0

    /*
    留存差额数
     */
    @NotBlank(message = "留存差额数不能为空", groups = {AddGroup.class})
    private BigDecimal retainedDifference = BigDecimal.ZERO;       // 默认0

    /*
    备用金
     */
    @NotBlank(message = "备用金不能为空", groups = {AddGroup.class})
    private BigDecimal pettyCash = BigDecimal.ZERO;                // 默认0

    /*
    备注信息
     */
    private String remarks;



    /*
    报表日期
     */
    @NotBlank(message = "报表日期不能为空", groups = {AddGroup.class})
    private String reportDate;

    private String reportYear;

    private LocalDateTime createTime;


}
