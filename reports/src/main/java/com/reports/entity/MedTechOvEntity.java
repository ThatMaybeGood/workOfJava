package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 医技统计-概览
 */
@Data
@TableName("TR_MEDTECH_OV")
public class MedTechOvEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId("id")
    private Long id;

    /** 统计日期 */
    @TableField("stat_date")
    private Date statDate;

    /** 检查人次 */
    @TableField("check_count")
    private Integer checkCount;

    /** 准时率 */
    @TableField("on_time_rate")
    private String onTimeRate;

    /** 等候时长 */
    @TableField("wait_time")
    private String waitTime;

    /** 平均迟到 */
    @TableField("avg_wait_late")
    private String avgWaitLate;

    /** 平均报告时长 */
    @TableField("avg_report_time")
    private String avgReportTime;

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
