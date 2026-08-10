package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 门诊运行数据统计-概览（表：TR_OUTP_OP）
 */
@Data
@TableName("TR_OUTP_OP")
public class OutpatientOperationEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 统计日期（复合主键） */
    @TableField("stat_date")
    private Date statDate;

    /** 科室代码（复合主键） */
    @TableField("dept_code")
    private String deptCode;

    /** 科室名称 */
    @TableField("dept_name")
    private String deptName;

    /** 出诊单元（复合主键） */
    @TableField("unit")
    private String unit;

    /** 门诊量 */
    @TableField("total_visits")
    private Integer totalVisits;

    /** 出诊人次-名医专家 */
    @TableField("famous_expert")
    private Integer famousExpert;

    /** 出诊人次-特需专家 */
    @TableField("special_expert")
    private Integer specialExpert;

    /** 出诊人次-知名专家 */
    @TableField("known_expert")
    private Integer knownExpert;

    /** 出诊人次-专家A */
    @TableField("expert_a")
    private Integer expertA;

    /** 出诊人次-专家B */
    @TableField("expert_b")
    private Integer expertB;

    /** 出诊人次-普通门诊 */
    @TableField("ordinary")
    private Integer ordinary;

    /** 有效出诊单元-名医 */
    @TableField("unit_famous_effective")
    private Integer unitFamousEffective;

    /** 出诊单元-名医 */
    @TableField("unit_famous_total")
    private Integer unitFamousTotal;

    /** 有效出诊单元-特需 */
    @TableField("unit_special_effective")
    private Integer unitSpecialEffective;

    /** 出诊单元-特需 */
    @TableField("unit_special_total")
    private Integer unitSpecialTotal;

    /** 有效出诊单元-知名专家 */
    @TableField("unit_known_effective")
    private Integer unitKnownEffective;

    /** 出诊单元-知名专家 */
    @TableField("unit_known_total")
    private Integer unitKnownTotal;

    /** 有效出诊单元-专家A */
    @TableField("unit_a_effective")
    private Integer unitAEffective;

    /** 出诊单元-专家A */
    @TableField("unit_a_total")
    private Integer unitATotal;

    /** 有效出诊单元-专家B */
    @TableField("unit_b_effective")
    private Integer unitBEffective;

    /** 出诊单元-专家B */
    @TableField("unit_b_total")
    private Integer unitBTotal;

    /** 有效出诊单元-普通 */
    @TableField("unit_ordinary_effective")
    private Integer unitOrdinaryEffective;

    /** 出诊单元-普通 */
    @TableField("unit_ordinary_total")
    private Integer unitOrdinaryTotal;

    /** 预约总数 */
    @TableField("appointment_total")
    private Integer appointmentTotal;

    /** 预约数 */
    @TableField("appointment_count")
    private Integer appointmentCount;

    /** 退号数 */
    @TableField("return_visits")
    private Integer returnVisits;

    /** 检查检验开单数 */
    @TableField("treat_count")
    private Integer treatCount;

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
