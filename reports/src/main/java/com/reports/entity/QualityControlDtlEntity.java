package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 门诊管理质量控制-月度明细（一月一行，比率不落库）
 */
@Data
@TableName("TR_QC_DTL")
public class QualityControlDtlEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 统计月份(YYYY-MM) */
    @TableId("stat_month")
    private String statMonth;

    @TableField("emr_usage_rate_num")
    private BigDecimal emrUsageRateNum;

    @TableField("emr_usage_rate_den")
    private BigDecimal emrUsageRateDen;

    @TableField("standard_diagnosis_rate_num")
    private BigDecimal standardDiagnosisRateNum;

    @TableField("standard_diagnosis_rate_den")
    private BigDecimal standardDiagnosisRateDen;

    @TableField("on_time_rate_num")
    private BigDecimal onTimeRateNum;

    @TableField("on_time_rate_den")
    private BigDecimal onTimeRateDen;

    @TableField("stop_rate_num")
    private BigDecimal stopRateNum;

    @TableField("stop_rate_den")
    private BigDecimal stopRateDen;

    @TableField("chemo_record_rate_num")
    private BigDecimal chemoRecordRateNum;

    @TableField("chemo_record_rate_den")
    private BigDecimal chemoRecordRateDen;

    @TableField("chemo_adverse_rate_num")
    private BigDecimal chemoAdverseRateNum;

    @TableField("chemo_adverse_rate_den")
    private BigDecimal chemoAdverseRateDen;

    @TableField("chemo_infusion_rate_num")
    private BigDecimal chemoInfusionRateNum;

    @TableField("chemo_infusion_rate_den")
    private BigDecimal chemoInfusionRateDen;

    @TableField("critical_value_rate_num")
    private BigDecimal criticalValueRateNum;

    @TableField("critical_value_rate_den")
    private BigDecimal criticalValueRateDen;

    @TableField("blood_draw_error_rate_num")
    private BigDecimal bloodDrawErrorRateNum;

    @TableField("blood_draw_error_rate_den")
    private BigDecimal bloodDrawErrorRateDen;

    @TableField("surgery_complication_rate_num")
    private BigDecimal surgeryComplicationRateNum;

    @TableField("surgery_complication_rate_den")
    private BigDecimal surgeryComplicationRateDen;

    @TableField("adverse_event_rate_num")
    private BigDecimal adverseEventRateNum;

    @TableField("adverse_event_rate_den")
    private BigDecimal adverseEventRateDen;

    /** 数据来源(人工登记/ETL抽取) */
    @TableField("source_type")
    private String sourceType;

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
