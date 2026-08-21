package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 门诊财务报表-抽取：门诊收据明细（ETL 按就诊日期抽取，源表 outp_rcpt_master）
 */
@Data
@TableName("ETL_OUTP_RCPT")
public class EtlOutpRcptEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId("id")
    private Long id;

    /** 收据号（人次去重解析前缀+序号、关联支付表） */
    @TableField("rcpt_no")
    private String rcptNo;

    /** 患者ID（人次去重） */
    @TableField("patient_id")
    private String patientId;

    /** 就诊日期（抽取日期、当期/同期判定） */
    @TableField("visit_date")
    private Date visitDate;

    /** 金额（bt6 渠道金额、bt8 业务类型金额） */
    @TableField("total_charges")
    private BigDecimal totalCharges;

    /** 退费关联收据号（退项 T3 筛选） */
    @TableField("refunded_rcpt_no")
    private String refundedRcptNo;

    /** 操作员号（bt1/3/4/6 operator 归类） */
    @TableField("operator_no")
    private String operatorNo;

    /** 单据类别（bt8 分界后 挂号/缴费 判定） */
    @TableField("bill_class")
    private String billClass;

    /** 创建时间 */
    @TableField("create_time")
    private Date createTime;

    /** 更新时间 */
    @TableField("update_time")
    private Date updateTime;
}
