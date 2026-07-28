package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 门诊运行数据统计-科室明细
 */
@Data
@TableName("TR_OUTP_OP_DTL")
public class OutpatientOpDtlEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("id")
    /**
     * 主键ID
     */
    private Long id;

    @TableField("stat_date")
    /**
     * 统计日期
     */
    private Date statDate;

    @TableField("dept_name")
    /**
     * 科室名称
     */
    private String deptName;

    @TableField("visits")
    /**
     * 就诊人次
     */
    private Integer visits;

    @TableField("appointment_rate")
    /**
     * 预约率
     */
    private String appointmentRate;

    @TableField("exam_rate")
    /**
     * 检查率
     */
    private String examRate;

    @TableField("efficiency")
    /**
     * 效率
     */
    private BigDecimal efficiency;

    @TableField("visit_count")
    /**
     * 就诊人次统计
     */
    private Integer visitCount;

    @TableField("famous_expert")
    /**
     * 名医就诊人次
     */
    private Integer famousExpert;

    @TableField("special_expert")
    /**
     * 特需专家就诊人次
     */
    private Integer specialExpert;

    @TableField("known_expert")
    /**
     * 知名专家就诊人次
     */
    private Integer knownExpert;

    @TableField("expert_a")
    /**
     * 专家A就诊人次
     */
    private Integer expertA;

    @TableField("expert_b")
    /**
     * 专家B就诊人次
     */
    private Integer expertB;

    @TableField("ordinary")
    /**
     * 普通就诊人次
     */
    private Integer ordinary;

    @TableField("effective_total")
    /**
     * 有效单元总数
     */
    private Integer effectiveTotal;

    @TableField("effective_detail")
    /**
     * 有效单元明细
     */
    private Integer effectiveDetail;

    @TableField("total_detail")
    /**
     * 总单元明细
     */
    private Integer totalDetail;

    @TableField("create_time")
    /**
     * 创建时间
     */
    private Date createTime;

    @TableField("update_time")
    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField("ext1")
    /**
     * 扩展字段1
     */
    private String ext1;

    @TableField("ext2")
    /**
     * 扩展字段2
     */
    private String ext2;

    @TableField("ext3")
    /**
     * 扩展字段3
     */
    private String ext3;
}
