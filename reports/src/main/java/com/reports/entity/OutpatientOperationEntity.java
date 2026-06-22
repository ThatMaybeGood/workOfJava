package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 门诊运行数据统计-概览
 */
@Data
@TableName("TR_OUTP_OP")
public class OutpatientOperationEntity implements Serializable {

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
     * 科室代码
     */
    @TableField(value = "dept_code")
    private String deptCode;

    /**
     * 科室名称
     */
    @TableField(value = "dept_name")
    private String deptName;

    /**
     * 门诊量
     */
    @TableField(value = "total_visits")
    private Integer totalVisits;

    /**
     * 预约挂号率
     */
    @TableField(value = "appointment_rate")
    private String appointmentRate;

    /**
     * 出诊人次
     */
    @TableField(value = "visit_count")
    private Integer visitCount;

    /**
     * 预约诊察查率
     */
    @TableField(value = "exam_rate")
    private String examRate;

    /**
     * 接诊效率
     */
    @TableField(value = "efficiency")
    private BigDecimal efficiency;

    /**
     * 有效出诊单元数
     */
    @TableField(value = "effective_units")
    private Integer effectiveUnits;

    /**
     * 出诊单元
     */
    @TableField(value = "total_units")
    private Integer totalUnits;

    /**
     * 出诊人次-名医专家
     */
    @TableField(value = "famous_expert")
    private Integer famousExpert;

    /**
     * 出诊人次-特需专家
     */
    @TableField(value = "special_expert")
    private Integer specialExpert;

    /**
     * 出诊人次-知名专家
     */
    @TableField(value = "known_expert")
    private Integer knownExpert;

    /**
     * 出诊人次-专家A
     */
    @TableField(value = "expert_a")
    private Integer expertA;

    /**
     * 出诊人次-专家B
     */
    @TableField(value = "expert_b")
    private Integer expertB;

    /**
     * 出诊人次-普通门诊
     */
    @TableField(value = "ordinary")
    private Integer ordinary;

    /**
     * 有效出诊单元-名医
     */
    @TableField(value = "unit_famous_effective")
    private Integer unitFamousEffective;

    /**
     * 出诊单元-名医
     */
    @TableField(value = "unit_famous_total")
    private Integer unitFamousTotal;

    /**
     * 有效出诊单元-特需
     */
    @TableField(value = "unit_special_effective")
    private Integer unitSpecialEffective;

    /**
     * 出诊单元-特需
     */
    @TableField(value = "unit_special_total")
    private Integer unitSpecialTotal;

    /**
     * 有效出诊单元-知名专家
     */
    @TableField(value = "unit_known_effective")
    private Integer unitKnownEffective;

    /**
     * 出诊单元-知名专家
     */
    @TableField(value = "unit_known_total")
    private Integer unitKnownTotal;

    /**
     * 有效出诊单元-专家A
     */
    @TableField(value = "unit_a_effective")
    private Integer unitAEffective;

    /**
     * 出诊单元-专家A
     */
    @TableField(value = "unit_a_total")
    private Integer unitATotal;

    /**
     * 有效出诊单元-专家B
     */
    @TableField(value = "unit_b_effective")
    private Integer unitBEffective;

    /**
     * 出诊单元-专家B
     */
    @TableField(value = "unit_b_total")
    private Integer unitBTotal;

    /**
     * 有效出诊单元-普通
     */
    @TableField(value = "unit_ordinary_effective")
    private Integer unitOrdinaryEffective;

    /**
     * 出诊单元-普通
     */
    @TableField(value = "unit_ordinary_total")
    private Integer unitOrdinaryTotal;

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