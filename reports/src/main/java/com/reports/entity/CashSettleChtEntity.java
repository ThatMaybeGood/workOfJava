package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 收费员结账统计-图表
 */
@Data
@TableName("TR_CASH_SETTLE_CHT")
public class CashSettleChtEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId("id")
    private Long id;

    /** 统计日期 */
    @TableField("stat_date")
    private Date statDate;

    /** 图表标题 */
    @TableField("chart_title")
    private String chartTitle;

    /** 副标题 */
    @TableField("chart_subtitle")
    private String chartSubtitle;

    /** 日期范围 */
    @TableField("date_range")
    private String dateRange;

    /** 分类 */
    @TableField("category")
    private String category;

    /** 数值 */
    @TableField("data_value")
    private Integer dataValue;

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
