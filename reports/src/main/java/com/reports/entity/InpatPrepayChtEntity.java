package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 住院预交金统计-图表数据
 */
@Data
@TableName("TR_INPAT_PREPAY_CHT")
public class InpatPrepayChtEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId("id")
    private Long id;

    /** 统计日期 */
    @TableField("stat_date")
    private Date statDate;

    /** 图表类型(TREND/CHANNEL/PAY_TYPE) */
    @TableField("chart_type")
    private String chartType;

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

    /** 系列名称 */
    @TableField("series_name")
    private String seriesName;

    /** 数值 */
    @TableField("data_value")
    private Integer dataValue;

    /** 对比值 */
    @TableField("compare_value")
    private Integer compareValue;

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
