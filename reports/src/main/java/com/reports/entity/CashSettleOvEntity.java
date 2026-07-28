package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 收费员结账统计-概览
 */
@Data
@TableName("TR_CASH_SETTLE_OV")
public class CashSettleOvEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId("id")
    private Long id;

    /** 统计日期 */
    @TableField("stat_date")
    private Date statDate;

    /** 预约挂号 */
    @TableField("appointment_register")
    private Integer appointmentRegister;

    /** 预约挂号对比 */
    @TableField("appointment_register_compare")
    private Integer appointmentRegisterCompare;

    /** 预约取号 */
    @TableField("appointment_fetch")
    private Integer appointmentFetch;

    /** 预约取号对比 */
    @TableField("appointment_fetch_compare")
    private Integer appointmentFetchCompare;

    /** 当日挂号 */
    @TableField("today_register")
    private Integer todayRegister;

    /** 当日挂号对比 */
    @TableField("today_register_compare")
    private Integer todayRegisterCompare;

    /** 退号 */
    @TableField("refund")
    private Integer refund;

    /** 退号对比 */
    @TableField("refund_compare")
    private Integer refundCompare;

    /** 门诊收费 */
    @TableField("outpatient_charge")
    private Integer outpatientCharge;

    /** 门诊收费对比 */
    @TableField("outpatient_charge_compare")
    private Integer outpatientChargeCompare;

    /** 门诊退费 */
    @TableField("outpatient_refund")
    private Integer outpatientRefund;

    /** 门诊退费对比 */
    @TableField("outpatient_refund_compare")
    private Integer outpatientRefundCompare;

    /** 预交金 */
    @TableField("prepayment")
    private Integer prepayment;

    /** 预交金对比 */
    @TableField("prepayment_compare")
    private Integer prepaymentCompare;

    /** 住院退费 */
    @TableField("hospital_refund")
    private Integer hospitalRefund;

    /** 住院退费对比 */
    @TableField("hospital_refund_compare")
    private Integer hospitalRefundCompare;

    /** 出院结算 */
    @TableField("discharge_settlement")
    private Integer dischargeSettlement;

    /** 出院结算对比 */
    @TableField("discharge_settlement_compare")
    private Integer dischargeSettlementCompare;

    /** 创建时间 */
    @TableField("create_time")
    private Date createTime;

    /** 更新时间 */
    @TableField("update_time")
    private Date updateTime;

    /** 扩展字段1 */
    @TableField("ext1")
    private String ext1;

    /** 扩展字段2 */
    @TableField("ext2")
    private String ext2;

    /** 扩展字段3 */
    @TableField("ext3")
    private String ext3;
}
