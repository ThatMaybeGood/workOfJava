package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 预测门诊量报表-概览
 */
@Data
@TableName("TR_OUTP_FC_OV")
public class OutpatientForecastOvEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("id")
    /**
     * 主键ID
     */
    private Long id;

    @TableField("stat_date")
    /**
     * 统计日期
     */
    private Date statDate;

    @TableField("tomorrow")
    /**
     * 明日预测
     */
    private Integer tomorrow;

    @TableField("next_week")
    /**
     * 下周预测
     */
    private Integer nextWeek;

    @TableField("next_month")
    /**
     * 下月预测
     */
    private Integer nextMonth;

    @TableField("next_year")
    /**
     * 明年预测
     */
    private Integer nextYear;

    @TableField("create_time")
    /**
     * 创建时间
     */
    private Date createTime;

    @TableField("update_time")
    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField("ext1")
    /**
     * 扩展字段1
     */
    private String ext1;

    @TableField("ext2")
    /**
     * 扩展字段2
     */
    private String ext2;

    @TableField("ext3")
    /**
     * 扩展字段3
     */
    private String ext3;
}
