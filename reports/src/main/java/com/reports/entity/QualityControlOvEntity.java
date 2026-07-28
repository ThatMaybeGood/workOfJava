package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 门诊管理质量控制-概览
 */
@Data
@TableName("TR_QC_OV")
public class QualityControlOvEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId("id")
    private Long id;

    /** 统计日期 */
    @TableField("stat_date")
    private Date statDate;

    /** 病历使用率 */
    @TableField("emr_usage_rate")
    private String emrUsageRate;

    /** 规范诊断率 */
    @TableField("standard_diagnosis_rate")
    private String standardDiagnosisRate;

    /** 准时率 */
    @TableField("on_time_rate")
    private String onTimeRate;

    /** 停诊率 */
    @TableField("stop_rate")
    private String stopRate;

    /** 化疗记录率 */
    @TableField("chemo_record_rate")
    private String chemoRecordRate;

    /** 化疗不良反应率 */
    @TableField("chemo_adverse_rate")
    private String chemoAdverseRate;

    /** 化疗输液率 */
    @TableField("chemo_infusion_rate")
    private String chemoInfusionRate;

    /** 危急值处理率 */
    @TableField("critical_value_rate")
    private String criticalValueRate;

    /** 抽血差错率 */
    @TableField("blood_draw_error_rate")
    private String bloodDrawErrorRate;

    /** 手术并发症率 */
    @TableField("surgery_complication_rate")
    private String surgeryComplicationRate;

    /** 不良事件率 */
    @TableField("adverse_event_rate")
    private String adverseEventRate;

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
