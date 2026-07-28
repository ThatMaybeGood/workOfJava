package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 住院预交金统计-概览
 */
@Data
@TableName("TR_INPAT_PREPAY_OV")
public class InpatPrepayOvEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId("id")
    private Long id;

    /** 统计日期 */
    @TableField("stat_date")
    private Date statDate;

    /** 预交金笔数 */
    @TableField("prepayment_count")
    private Integer prepaymentCount;

    /** 预交金笔数对比值 */
    @TableField("prepayment_count_compare")
    private Integer prepaymentCountCompare;

    /** 预交金金额 */
    @TableField("prepayment_amount")
    private BigDecimal prepaymentAmount;

    /** 预交金金额对比值 */
    @TableField("prepayment_amount_compare")
    private Integer prepaymentAmountCompare;

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
