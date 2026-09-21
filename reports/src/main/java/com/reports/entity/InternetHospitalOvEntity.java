package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 互医质控运营月报-概览
 */
@Data
@TableName("TR_INET_HOSP_OV")
public class InternetHospitalOvEntity implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 统计日期(YYYY-MM-DD)
     */
    @TableField("stat_date")
    private Date statDate;

    /**
     * 门诊量
     */
    @TableField("outpatient_volume")
    private Integer outpatientVolume;

    /** 互联网医师占比-分子 */
    @TableField("doctor_ratio_num")
    private BigDecimal doctorRatioNum;

    /** 互联网医师占比-分母 */
    @TableField("doctor_ratio_den")
    private BigDecimal doctorRatioDen;

    /** 互联网医院接诊率-分子 */
    @TableField("reception_rate_num")
    private BigDecimal receptionRateNum;

    /** 互联网医院接诊率-分母 */
    @TableField("reception_rate_den")
    private BigDecimal receptionRateDen;

    /** 互联网医院处方开具率-分子 */
    @TableField("prescription_rate_num")
    private BigDecimal prescriptionRateNum;

    /** 互联网医院处方开具率-分母 */
    @TableField("prescription_rate_den")
    private BigDecimal prescriptionRateDen;

    /** 互联网医院病历书写率-分子 */
    @TableField("record_rate_num")
    private BigDecimal recordRateNum;

    /** 互联网医院病历书写率-分母 */
    @TableField("record_rate_den")
    private BigDecimal recordRateDen;

    /** 互联网医院处方点评率-分子 */
    @TableField("review_rate_num")
    private BigDecimal reviewRateNum;

    /** 互联网医院处方点评率-分母 */
    @TableField("review_rate_den")
    private BigDecimal reviewRateDen;

    /** 互联网医院药品处方执行率-分子 */
    @TableField("execution_rate_num")
    private BigDecimal executionRateNum;

    /** 互联网医院药品处方执行率-分母 */
    @TableField("execution_rate_den")
    private BigDecimal executionRateDen;

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
