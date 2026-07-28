package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 互医质控运营月报-概览
 */
@Data
@TableName("TR_INET_HOSP_OV")
public class InternetHospitalOvEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId("id")
    private Long id;

    /**
     * 统计月份(YYYY-MM)
     */
    @TableField("stat_month")
    private String statMonth;

    /**
     * 门诊量
     */
    @TableField("outpatient_volume")
    private Integer outpatientVolume;

    /**
     * 医师占比
     */
    @TableField("doctor_ratio")
    private String doctorRatio;

    /**
     * 接诊率
     */
    @TableField("reception_rate")
    private String receptionRate;

    /**
     * 处方率
     */
    @TableField("prescription_rate")
    private String prescriptionRate;

    /**
     * 病历率
     */
    @TableField("record_rate")
    private String recordRate;

    /**
     * 审方率
     */
    @TableField("review_rate")
    private String reviewRate;

    /**
     * 执行率
     */
    @TableField("execution_rate")
    private String executionRate;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;

    /**
     * 扩展字段1
     */
    @TableField("ext1")
    private String ext1;

    /**
     * 扩展字段2
     */
    @TableField("ext2")
    private String ext2;

    /**
     * 扩展字段3
     */
    @TableField("ext3")
    private String ext3;
}
