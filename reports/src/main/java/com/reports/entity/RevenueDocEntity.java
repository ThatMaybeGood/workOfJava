package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 门诊收入分析-医生明细（数据来源：TR_REV_OV 按医生聚合）
 */
@Data
@TableName("TR_REV_OV")
public class RevenueDocEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId("id")
    private Long id;

    /** 统计日期 */
    @TableField("stat_date")
    private Date statDate;

    /** 医生姓名 */
    @TableField("doctor_name")
    private String doctorName;

    /** 科室名称 */
    @TableField("dept_name")
    private String deptName;

    /** 挂号收入 */
    @TableField("register_revenue")
    private String registerRevenue;

    /** 药品收入（缴费） */
    @TableField("medical_revenue")
    private String medicalRevenue;

    /** 医生效益（挂号+药品+服务性） */
    @TableField("doctor_benefit")
    private String doctorBenefit;

    /** 服务性收入 */
    @TableField("service_revenue")
    private String serviceRevenue;

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
