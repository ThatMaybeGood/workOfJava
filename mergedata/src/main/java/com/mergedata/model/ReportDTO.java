package com.mergedata.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class ReportDTO {

    private String serialNo;

    // 操作员基础信息
    private String operatorNo;
    private String operatorName;

    // HisData 相关字段
    /*
    his预交金
     */
    private BigDecimal hisAdvancePayment = BigDecimal.ZERO;

    /*
    his医疗收入
     */
    private BigDecimal hisMedicalIncome = BigDecimal.ZERO;

    /*
    his挂号收入  暂时默认0
    */
    private BigDecimal hisRegistrationIncome = BigDecimal.ZERO;


    // YQCashRegRecord 相关字段
    /*
    留存现金数
     */
    private BigDecimal retainedCash = BigDecimal.ZERO;
    private String windowNo;
    private String operatType;
    private String sechduling;
    private String applyDate;


    // 其他报表字段（根据您提供的字段）

    /*
    应交报表数
     */
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
    private BigDecimal actualReportAmount = BigDecimal.ZERO;       // 默认0

    /*
    当日暂收款
     */
    private BigDecimal currentTemporaryReceipt = BigDecimal.ZERO;  // 默认0

    /*
    实收现金数
     */
    private BigDecimal actualCashAmount = BigDecimal.ZERO;         // 默认0

    /*
    留存差额数
     */
    private BigDecimal retainedDifference = BigDecimal.ZERO;       // 默认0

    /*
    备用金
     */
    private BigDecimal pettyCash = BigDecimal.ZERO;                // 默认0

    /*
    备注信息
     */
    private String remarks;


    private String reportDate;

    private LocalDateTime createTime;


}
