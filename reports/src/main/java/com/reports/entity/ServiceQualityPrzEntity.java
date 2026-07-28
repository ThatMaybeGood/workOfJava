package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 门诊服务质量分析-表扬明细
 */
@Data
@TableName("TR_SVC_QUALITY_PRZ")
public class ServiceQualityPrzEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId("id")
    private Long id;

    /** 统计日期 */
    @TableField("stat_date")
    private Date statDate;

    /** 表扬时间 */
    @TableField("praise_time")
    private Date praiseTime;

    /** 科室 */
    @TableField("dept_name")
    private String deptName;

    /** 人员 */
    @TableField("person_name")
    private String personName;

    /** 职位 */
    @TableField("position")
    private String position;

    /** 表扬方式 */
    @TableField("method")
    private String method;

    /** 反馈内容 */
    @TableField("feedback")
    private String feedback;

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
