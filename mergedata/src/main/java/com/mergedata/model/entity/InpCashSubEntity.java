package com.mergedata.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mergedata.util.AddGroup;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 住院现金统计子表实体类
 */
@Data
@TableName("mpp_cash_inp_sub") // 真实的表名
public class InpCashSubEntity {

    // 基础信息
    @TableField("serial_no")
    private String serialNo;// ID
    @TableField("emp_id")
    private String operatorNo; // 收费员ID
    @TableField("emp_name")
    private String operatorName; // 收费员姓名


    // 上午统计报表现金数据字段  金额字段
    @Getter(AccessLevel.NONE)
    @TableField("prev_day_adv_receipt")
    private BigDecimal previousDayAdvanceReceipt;    //1 前日暂收款   正常工作日 获取前一天12的值，节假日获取 12的节假日前一天值

    @Getter(AccessLevel.NONE)
    @NotBlank(message = "his今日预交金数不能为空", groups = {AddGroup.class})
    @TableField("today_adv_payment")
    private BigDecimal todayAdvancePayment;          //2 his今日预交金数

    @Getter(AccessLevel.NONE)
    @NotBlank(message = "his今日结账收入不能为空", groups = {AddGroup.class})
    @TableField("today_settle_income")
    private BigDecimal todaySettlementIncome;        //3 his今日结账收入

    @Getter(AccessLevel.NONE)
    @NotBlank(message = "his今日院前收入不能为空", groups = {AddGroup.class})
    @TableField("today_pre_hosp_income")
    private BigDecimal todayPreHospitalIncome;       //4 his今日院前收入

    @Getter(AccessLevel.NONE)
    @TableField("traffic_assist_fund")
    private BigDecimal trafficAssistanceFund;        //5 交通救助金

    @Getter(AccessLevel.NONE)
    @TableField("blood_donate_compensate")
    private BigDecimal bloodDonationCompensation;    //6 献血补偿金

    @Getter(AccessLevel.NONE)
    @TableField("receivable_payable")
    private BigDecimal receivablePayable;            //7 应收款/应付款

    @Getter(AccessLevel.NONE)
    @TableField("today_report_total")
    private BigDecimal todayReportTotal;             //8 =（2）-（1）+（3）+（4）+（5）+(6)+(7) 今日报表数合计

    @Getter(AccessLevel.NONE)
    @TableField("prev_day_iou")
    private BigDecimal previousDayIOU;               //9 前日欠条

    @Getter(AccessLevel.NONE)
    @TableField("today_outp_iou")
    private BigDecimal todayOutpatientIOU;           //10 今日门诊借条

    @Getter(AccessLevel.NONE)
    @TableField("today_report_rec_pay")
    private BigDecimal todayReportReceivablePayable; //（11）=（8）+（9）+（10）-（18） 今日报表应收/应付


    /**
     * 下午 收取现金数据
     */
    @Getter(AccessLevel.NONE)
    @TableField("today_adv_receipt")
    private BigDecimal todayAdvanceReceipt;       //（12）=（8）-（11） 今日暂收款

    @Getter(AccessLevel.NONE)
    @TableField("today_report_cash_rcv")
    private BigDecimal todayReportCashReceived;      //13 今日报表实收

    @Getter(AccessLevel.NONE)
    @TableField("today_cash_rcv_total")
    private BigDecimal todayCashReceivedTotal;       //（14）=（12）+（13） 今日实收现金合计

    @Getter(AccessLevel.NONE)
    @TableField("balance")
    private BigDecimal balance;                      //（15）=（13）-（11）余额

    @Getter(AccessLevel.NONE)
    @TableField("adjustment")
    private BigDecimal adjustment;                   //16 调整

    @Getter(AccessLevel.NONE)
    @TableField("today_iou")
    private BigDecimal todayIOU;                     //（17）=（16）-（15） 今日欠条

    @Getter(AccessLevel.NONE)
    @TableField("holiday_payment")
    private BigDecimal holidayPayment;               //18 节假日交款


    /**
     * 收费员留存字段
     */
    @Getter(AccessLevel.NONE)
    @TableField("cash_on_hand")
    private BigDecimal cashOnHand;                   //19 库存现金

    @Getter(AccessLevel.NONE)
    @TableField("difference")
    private BigDecimal difference;                   //（20）=（19）-（11）  差额

    @Getter(AccessLevel.NONE)
    @TableField("remarks")
    private String remarks;                      //21  备注


    /*
     * 其他字段
     */
    @TableField("created_time")
    private LocalDateTime createdTime;  //创建时间
    @TableField("updated_time")
    private LocalDateTime updatedTime;  //更新时间
    @TableField("created_by")
    private String createdBy;   //创建人
    @TableField("updated_by")
    private String updatedBy;   //更新人


    //  重写 Getter 方法
    public BigDecimal getPreviousDayAdvanceReceipt() { return safeBigDecimal(previousDayAdvanceReceipt); }
    public BigDecimal getTodayAdvancePayment() { return safeBigDecimal(todayAdvancePayment); }
    public BigDecimal getTodaySettlementIncome() { return safeBigDecimal(todaySettlementIncome); }
    public BigDecimal getTodayPreHospitalIncome() { return safeBigDecimal(todayPreHospitalIncome); }
    public BigDecimal getTrafficAssistanceFund() { return safeBigDecimal(trafficAssistanceFund); }
    public BigDecimal getBloodDonationCompensation() { return safeBigDecimal(bloodDonationCompensation); }
    public BigDecimal getReceivablePayable() { return safeBigDecimal(receivablePayable); }
    public BigDecimal getTodayReportTotal() { return safeBigDecimal(todayReportTotal); }
    public BigDecimal getPreviousDayIOU() { return safeBigDecimal(previousDayIOU); }
    public BigDecimal getTodayOutpatientIOU() { return safeBigDecimal(todayOutpatientIOU); }
    public BigDecimal getTodayReportReceivablePayable() { return safeBigDecimal(todayReportReceivablePayable); }
    public BigDecimal getTodayAdvanceReceipt() { return safeBigDecimal(todayAdvanceReceipt); }
    public BigDecimal getTodayReportCashReceived() { return safeBigDecimal(todayReportCashReceived); }
    public BigDecimal getTodayCashReceivedTotal() { return safeBigDecimal(todayCashReceivedTotal); }
    public BigDecimal getBalance() { return safeBigDecimal(balance); }
    public BigDecimal getAdjustment() { return safeBigDecimal(adjustment); }
    public BigDecimal getTodayIOU() { return safeBigDecimal(todayIOU); }
    public BigDecimal getHolidayPayment() { return safeBigDecimal(holidayPayment); }
    public BigDecimal getCashOnHand() { return safeBigDecimal(cashOnHand); }
    public BigDecimal getDifference() { return safeBigDecimal(difference); }


    /*
     *  统一BigDecimal类型的null值
     */
     private BigDecimal safeBigDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }





}
