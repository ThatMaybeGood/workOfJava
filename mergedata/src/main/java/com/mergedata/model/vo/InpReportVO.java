package com.mergedata.model.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 数据库映射实体类 - 对应整张表
public class InpReportVO {

    // 基础信息
    private Long id;                            // 主键ID
    private String reportDate;                  // 报表日期
    private String registrationTime;            // 登记时间
    private String sheetType;                   // 表类型：平时/节假日
    private String cashier;                     // 收费员姓名
    private Integer serialNumber;               // 序号

    // 上午统计报表现金数据字段
    private BigDecimal previousDayAdvanceReceipt;    // 前日暂收款
    private BigDecimal todayAdvancePayment;          // 今日预交金数
    private BigDecimal todaySettlementIncome;        // 今日结账收入
    private BigDecimal todayPreHospitalIncome;       // 今日院前收入
    private BigDecimal trafficAssistanceFund;        // 交通救助金
    private BigDecimal bloodDonationCompensation;    // 无偿献血补偿金
    private BigDecimal receivablePayable;            // 应收款/应付款
    private BigDecimal todayReportTotal;             // 今日报表数合计
    private BigDecimal previousDayIOU;               // 前日欠条
    private BigDecimal todayOutpatientIOU;           // 今日门诊借条
    private BigDecimal todayReportReceivablePayable; // 今日报表应收/应付

    // 下午收取现金数据字段
    private BigDecimal todayAdvanceReceipt;          // 今日暂收款
    private BigDecimal todayReportCashReceived;      // 今日报表实收
    private BigDecimal todayCashReceivedTotal;       // 今日实收现金合计
    private BigDecimal balance;                      // 余额
    private BigDecimal adjustment;                   // 调整
    private BigDecimal todayIOU;                     // 今日欠条
    private BigDecimal holidayPayment;               // 节假日交款

    // 收费员留存字段
    private BigDecimal cashOnHand;                   // 库存现金
    private BigDecimal difference;                   // 差额
    private String remarks;                      // 备注

    // 创建时间和更新时间
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // getter和setter...
}