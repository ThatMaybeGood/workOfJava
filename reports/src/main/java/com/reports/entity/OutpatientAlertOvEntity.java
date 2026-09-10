package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 门诊预警统计（单表：日期+科室+医生粒度，概览/科室表/医生表/早退明细均由此聚合）
 */
@Data
@TableName("TR_OUTP_ALT_OV")
public class OutpatientAlertOvEntity implements Serializable {

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

    @TableField("dept_code")
    /**
     * 科室编码
     */
    private String deptCode;

    @TableField("dept_name")
    /**
     * 科室名称
     */
    private String deptName;

    @TableField("doctor_name")
    /**
     * 医生姓名（无医生维度的预警为空）
     */
    private String doctorName;

    @TableField("remain_alert")
    /**
     * 当日余号预警次数
     */
    private Integer remainAlert;

    @TableField("appointment_alert")
    /**
     * 号源预约预警次数
     */
    private Integer appointmentAlert;

    @TableField("early_leave")
    /**
     * 早退次数
     */
    private Integer earlyLeave;

    @TableField("clinic_period")
    /**
     * 出诊时间段（早退明细用）
     */
    private String clinicPeriod;

    @TableField("his_logout_time")
    /**
     * HIS工作站最后登出时间（早退明细用）
     */
    private String hisLogoutTime;

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
