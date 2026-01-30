package com.mergedata.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 住院现金统计子表实体类
 */
@Data
@TableName("mpp_cash_statistics_inp_sub") // 只要在这里指定数据库真实的表名
public class InpCashStatisticsSubEntity {
    // 基础信息
    private String serialNo;// 主键ID
    @TableField("emp_id")
    private String hisOperatorNo; // 收费员ID
    @TableField("emp_name")
    private String hisOperatorName; // 收费员姓名


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



    // 私有方法：统一处理BigDecimal类型的null值
    private BigDecimal safeBigDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }


    // getters and setters

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getHisOperatorNo() {
        return hisOperatorNo;
    }

    public void setHisOperatorNo(String hisOperatorNo) {
        this.hisOperatorNo = hisOperatorNo;
    }

    public String getHisOperatorName() {
        return hisOperatorName;
    }

    public void setHisOperatorName(String hisOperatorName) {
        this.hisOperatorName = hisOperatorName;
    }

}
