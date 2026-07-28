package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 门诊预警统计-科室明细
 */
@Data
@TableName("TR_OUTP_ALT_DEPT")
public class OutpatientAlertDeptEntity implements Serializable {

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

    @TableField("remain_alert")
    /**
     * 滞留预警
     */
    private Integer remainAlert;

    @TableField("appointment_alert")
    /**
     * 预约预警
     */
    private Integer appointmentAlert;

    @TableField("early_leave")
    /**
     * 早退人数
     */
    private Integer earlyLeave;

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
