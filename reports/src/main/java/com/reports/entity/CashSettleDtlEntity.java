package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 收费员结账统计-日明细
 */
@Data
@TableName("TR_CASH_SETTLE_DTL")
public class CashSettleDtlEntity implements Serializable {

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

    /** 收费员 */
    @TableField("cashier_name")
    private String cashierName;

    /** 项目类型 */
    @TableField("item_type")
    private String itemType;

    /** 金额 */
    @TableField("item_value")
    private BigDecimal itemValue;

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
