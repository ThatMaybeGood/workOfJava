package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 治疗统计报表-科室明细
 */
@Data
@TableName("TR_TREAT_STAT_DTL")
public class TreatmentStatsDtlEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId("id")
    private Long id;

    /** 统计日期 */
    @TableField("stat_date")
    private Date statDate;

    /** 科室名称 */
    @TableField("dept_name")
    private String deptName;

    /** 患者人数 */
    @TableField("patient_count")
    private Integer patientCount;

    /** 治疗人次 */
    @TableField("treatment_count")
    private Integer treatmentCount;

    /** 治疗金额 */
    @TableField("treatment_amount")
    private BigDecimal treatmentAmount;

    /** 平均金额 */
    @TableField("avg_amount")
    private BigDecimal avgAmount;

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
