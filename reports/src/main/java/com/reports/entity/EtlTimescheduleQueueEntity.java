package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 门诊财务报表-抽取：排班队列（ETL 关联挂号按就诊日期抽取，源表 cq_card.mop_timeschedule_queue）
 * 仅 bt2 取号渠道分析使用。
 */
@Data
@TableName("TR_TIMESCHEDULE_QUEUE")
public class EtlTimescheduleQueueEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId("id")
    private Long id;

    /** 排班ID（与 clinic_label|yyyyMMdd|visit_time_desc 拼接比对） */
    @TableField("schedule_id")
    private String scheduleId;

    /** 患者ID（关联挂号） */
    @TableField("patient_id")
    private String patientId;

    /** 队列类型（筛选 'reserve'） */
    @TableField("queue_type")
    private String queueType;

    /** 是否使用（筛选 'used'） */
    @TableField("is_used")
    private String isUsed;

    /** 冗余就诊日期（抽取时关联挂号表补） */
    @TableField("visit_date")
    private Date visitDate;

    /** 创建时间 */
    @TableField("create_time")
    private Date createTime;

    /** 更新时间 */
    @TableField("update_time")
    private Date updateTime;
}
