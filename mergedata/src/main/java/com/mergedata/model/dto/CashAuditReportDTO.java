//package com.mergedata.model.dto;
//
//// 主报表类
//public class CashAuditReportDTO {
//
//    // 基础信息
//    private String reportDate;          // 报表日期，如："1月1日"
//    private String registrationTime;    // 登记时间，如："2026年1月2日"
//    private String sheetType;           // 表类型：平时/节假日
//
//    // 三个主要模块
//    private MorningReportData morningData;          // 上午统计报表现金数据
//    private AfternoonCollectionData afternoonData;  // 下午收取现金数据
//    private CashierRetainedData retainedData;       // 收费员留存
//
//    // 构造方法、getter和setter...
//}
//
//// 1. 上午统计报表现金数据
//public class MorningReportData {
//    private PreviousDayAdvanceReceipt previousDayAdvanceReceipt;  // 前日暂收款
//    private TodayAdvancePayment todayAdvancePayment;              // 今日预交金数
//    private TodaySettlementIncome todaySettlementIncome;          // 今日结账收入
//    private TodayPreHospitalIncome todayPreHospitalIncome;        // 今日院前收入
//    private TrafficAssistanceFund trafficAssistanceFund;          // 交通救助金
//    private BloodDonationCompensation bloodDonationCompensation;  // 无偿献血补偿金
//    private ReceivablePayable receivablePayable;                  // 应收款/应付款
//    private TodayReportTotal todayReportTotal;                    // 今日报表数合计
//    private PreviousDayIOU previousDayIOU;                        // 前日欠条
//    private TodayOutpatientIOU todayOutpatientIOU;                // 今日门诊借条
//    private TodayReportReceivablePayable todayReportReceivablePayable; // 今日报表应收/应付
//
//    // getter和setter...
//}
//
//// 2. 下午收取现金数据
//public class AfternoonCollectionData {
//    private TodayAdvanceReceipt todayAdvanceReceipt;              // 今日暂收款
//    private TodayReportCashReceived todayReportCashReceived;      // 今日报表实收
//    private TodayCashReceivedTotal todayCashReceivedTotal;        // 今日实收现金合计
//    private Balance balance;                                      // 余额
//    private Adjustment adjustment;                                // 调整
//    private TodayIOU todayIOU;                                    // 今日欠条
//    private HolidayPayment holidayPayment;                        // 节假日交款
//
//    // getter和setter...
//}
//
//// 3. 收费员留存
//public class CashierRetainedData {
//    private CashOnHand cashOnHand;                                // 库存现金
//    private Difference difference;                                // 差额
//    private String remarks;                                       // 备注
//
//    // getter和setter...
//}
//
//// ============ 子实体类 ============
//
//// 上午统计模块子类
//public class PreviousDayAdvanceReceipt {
//    private Double amount;
//    // getter/setter...
//}
//
//public class TodayAdvancePayment {
//    private Double amount;
//    // getter/setter...
//}
//
//public class TodaySettlementIncome {
//    private Double amount;
//    // getter/setter...
//}
//
//public class TodayPreHospitalIncome {
//    private Double amount;
//    // getter/setter...
//}
//
//public class TrafficAssistanceFund {
//    private Double amount;
//    // getter/setter...
//}
//
//public class BloodDonationCompensation {
//    private Double amount;
//    // getter/setter...
//}
//
//public class ReceivablePayable {
//    private Double amount;
//    // getter/setter...
//}
//
//public class TodayReportTotal {
//    private Double amount;
//    // getter/setter...
//}
//
//public class PreviousDayIOU {
//    private Double amount;
//    // getter/setter...
//}
//
//public class TodayOutpatientIOU {
//    private Double amount;
//    // getter/setter...
//}
//
//public class TodayReportReceivablePayable {
//    private Double amount;
//    // getter/setter...
//}
//
//// 下午收取模块子类
//public class TodayAdvanceReceipt {
//    private Double amount;
//    // getter/setter...
//}
//
//public class TodayReportCashReceived {
//    private Double amount;
//    // getter/setter...
//}
//
//public class TodayCashReceivedTotal {
//    private Double amount;
//    // getter/setter...
//}
//
//public class Balance {
//    private Double amount;
//    // getter/setter...
//}
//
//public class Adjustment {
//    private Double amount;
//    // getter/setter...
//}
//
//public class TodayIOU {
//    private Double amount;
//    // getter/setter...
//}
//
//public class HolidayPayment {
//    private Double amount;
//    // getter/setter...
//}
//
//// 收费员留存模块子类
//public class CashOnHand {
//    private Double amount;
//    // getter/setter...
//}
//
//public class Difference {
//    private Double amount;
//    // getter/setter...
//}