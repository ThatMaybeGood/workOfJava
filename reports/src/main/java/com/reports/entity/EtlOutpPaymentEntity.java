package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 门诊财务报表-抽取：门诊支付方式明细（ETL 关联收据按就诊日期抽取，源表 outp_payments_money）
 */
@Data
@TableName("ETL_OUTP_PAYMENT")
public class EtlOutpPaymentEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId("id")
    private Long id;

    /** 关联收据号 */
    @TableField("rcpt_no")
    private String rcptNo;

    /** 患者ID（bt5 人次支付按 同一患者同一收据 去重，抽取时关联收据表补） */
    @TableField("patient_id")
    private String patientId;

    /** 支付方式（bt5/7/9 类别） */
    @TableField("money_type")
    private String moneyType;

    /** 实收金额（bt7/9） */
    @TableField("payment_amount")
    private BigDecimal paymentAmount;

    /** 退费金额（bt7/9） */
    @TableField("refunded_amount")
    private BigDecimal refundedAmount;

    /** 冗余就诊日期（抽取时关联收据表补） */
    @TableField("visit_date")
    private Date visitDate;

    /** 创建时间 */
    @TableField("create_time")
    private Date createTime;

    /** 更新时间 */
    @TableField("update_time")
    private Date updateTime;
}
