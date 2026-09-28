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
    @TableField("other_income")
    private BigDecimal otherIncome;                  //5 其它收入-手工报表

    @TableField("sub_remark")
    private String subRemark;                    //6 备注（手工录入）

    @Getter(AccessLevel.NONE)
    @TableField("today_report_total")
    private BigDecimal todayReportTotal;             //7 =（2）-（1）+（3）+（4）+（5） 今日报表数合计

    @Getter(AccessLevel.NONE)
    @TableField("prev_day_iou")
    private BigDecimal previousDayIOU;               //8 前日欠条

    @Getter(AccessLevel.NONE)
    @TableField("today_outp_iou")
    private BigDecimal todayOutpatientIOU;           //9 今日借款

    @Getter(AccessLevel.NONE)
    @TableField("today_report_rec_pay")
    private BigDecimal todayReportReceivablePayable; //（10）=（7）+（8）+（9）-（17） 今日应收合计


    /**
     * 下午 收取现金数据
     */
    @Getter(AccessLevel.NONE)
    @TableField("today_adv_receipt")
    private BigDecimal todayAdvanceReceipt;       //（11）今日暂收款

    @Getter(AccessLevel.NONE)
    @TableField("today_report_cash_rcv")
    private BigDecimal todayReportCashReceived;      //12 今日报表实收

    @Getter(AccessLevel.NONE)
    @TableField("today_cash_rcv_total")
    private BigDecimal todayCashReceivedTotal;       //（13）=（11）+（12） 今日实收现金合计

    @Getter(AccessLevel.NONE)
    @TableField("balance")
    private BigDecimal balance;                      //（14）=（12）-（10）余额

    @Getter(AccessLevel.NONE)
    @TableField("adjustment")
    private BigDecimal adjustment;                   //15 调整

    @Getter(AccessLevel.NONE)
    @TableField("today_iou")
    private BigDecimal todayIOU;                     //（16）=（15）-（14） 今日欠条

    @Getter(AccessLevel.NONE)
    @TableField("holiday_payment")
    private BigDecimal holidayPayment;               //17 节假日交款


    /**
     * 收费员留存字段
     */
    @Getter(AccessLevel.NONE)
    @TableField("cash_on_hand")
    private BigDecimal cashOnHand;                   //18 库存现金

    @Getter(AccessLevel.NONE)
    @TableField("difference")
    private BigDecimal difference;                   //（19）=（18）-（10）-（17） 差额

    @TableField("remarks")
    private String remarks;                      //备注（手工录入）


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
    @TableField("db_user")
    private String dbUser;


    //  重写 Getter 方法
    public BigDecimal getPreviousDayAdvanceReceipt() { return safeBigDecimal(previousDayAdvanceReceipt); }
    public BigDecimal getTodayAdvancePayment() { return safeBigDecimal(todayAdvancePayment); }
    public BigDecimal getTodaySettlementIncome() { return safeBigDecimal(todaySettlementIncome); }
    public BigDecimal getTodayPreHospitalIncome() { return safeBigDecimal(todayPreHospitalIncome); }
    public BigDecimal getOtherIncome() { return safeBigDecimal(otherIncome); }
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
