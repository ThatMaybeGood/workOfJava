package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 门诊服务质量分析-投诉明细
 */
@Data
@TableName("TR_SVC_QUALITY_CMPL")
public class ServiceQualityCmplEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId("id")
    private Long id;

    /** 统计日期 */
    @TableField("stat_date")
    private Date statDate;

    /** 投诉时间 */
    @TableField("complaint_time")
    private Date complaintTime;

    /** 科室 */
    @TableField("dept_name")
    private String deptName;

    /** 人员 */
    @TableField("person_name")
    private String personName;

    /** 职位 */
    @TableField("position")
    private String position;

    /** 分类 */
    @TableField("category")
    private String category;

    /** 处理结果 */
    @TableField("result")
    private String result;

    /** 备注 */
    @TableField("remark")
    private String remark;

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
