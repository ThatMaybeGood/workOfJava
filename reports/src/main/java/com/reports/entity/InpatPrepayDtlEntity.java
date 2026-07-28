package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 住院预交金统计-日明细
 */
@Data
@TableName("TR_INPAT_PREPAY_DTL")
public class InpatPrepayDtlEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId("id")
    private Long id;

    /** 统计日期 */
    @TableField("stat_date")
    private Date statDate;

    /** 日期 */
    @TableField("item_date")
    private Date itemDate;

    /** 数据类型(SUMMARY/INCOME/REFUND) */
    @TableField("data_type")
    private String dataType;

    /** 上期笔数 */
    @TableField("count_last")
    private Integer countLast;

    /** 本期笔数 */
    @TableField("count_current")
    private Integer countCurrent;

    /** 笔数对比 */
    @TableField("count_compare")
    private Integer countCompare;

    /** 上期金额 */
    @TableField("amount_last")
    private BigDecimal amountLast;

    /** 本期金额 */
    @TableField("amount_current")
    private BigDecimal amountCurrent;

    /** 金额对比 */
    @TableField("amount_compare")
    private Integer amountCompare;

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
