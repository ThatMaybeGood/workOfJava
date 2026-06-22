package com.reports.entity;


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 患者画像-医保分析(按医保类型分析患者分布)
 */
@Data
@TableName("TR_PAT_PORTRAIT_INSUR")
public class OutpatientPatientPortraitInsurEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 统计日期
     */
    @TableField(value = "stat_date")
    private Date statDate;

    /**
     * 医保类型名称
     */
    @TableField(value = "insurance_type")
    private String insuranceType;

    /**
     * 医保类型名称
     */
    @TableField(value = "insurance_name")
    private String insuranceName;

    /**
     * 患者人数
     */
    @TableField(value = "patient_count")
    private Integer patientCount;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 扩展字段1
     */
    @TableField(value = "ext1")
    private String ext1;

    /**
     * 扩展字段2
     */
    @TableField(value = "ext2")
    private String ext2;

    /**
     * 扩展字段3
     */
    @TableField(value = "ext3")
    private String ext3;
}