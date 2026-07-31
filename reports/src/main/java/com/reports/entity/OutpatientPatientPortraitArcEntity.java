package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 患者画像-建档患者归属地分析
 */
@Data
@TableName("TR_PAT_PORTRAIT_ARC")
public class OutpatientPatientPortraitArcEntity implements Serializable {

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
     * 来源类型
     */
    @TableField(value = "source_type")
    private String sourceType;

    /**
     * 来源类型名称
     */
    @TableField(value = "source_name")
    private String sourceName;

    /**
     * 患者人数
     */
    @TableField(value = "patient_count")
    private Integer patientCount;

    /**
     * 患者类型：outpatient（门诊患者）、inpatient（住院患者）
     */
    @TableField(value = "patient_type")
    private String patientType;

    /**
     * 科室编码
     */
    @TableField(value = "dept_code")
    private String deptCode;

    /**
     * 科室名称
     */
    @TableField(value = "dept_name")
    private String deptName;

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