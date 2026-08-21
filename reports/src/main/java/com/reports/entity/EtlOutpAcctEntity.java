package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 门诊财务报表-抽取：门诊结账汇总（ETL 按结账日期抽取，源表 outp_acct_master）
 * 收据张数 / 金额的唯一口径。
 */
@Data
@TableName("ETL_OUTP_ACCT")
public class EtlOutpAcctEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId("id")
    private Long id;

    /** 结账日期（抽取日期、当期/同期判定） */
    @TableField("acct_date")
    private Date acctDate;

    /** 总费用（T1 金额） */
    @TableField("total_costs")
    private BigDecimal totalCosts;

    /** 退费金额（T3 金额） */
    @TableField("refund_amount")
    private BigDecimal refundAmount;

    /** 收据张数（T1） */
    @TableField("rcpts_num")
    private BigDecimal rcptsNum;

    /** 退费张数（T3） */
    @TableField("refund_num")
    private BigDecimal refundNum;

    /** 创建时间 */
    @TableField("create_time")
    private Date createTime;

    /** 更新时间 */
    @TableField("update_time")
    private Date updateTime;
}
