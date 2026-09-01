package com.reports.entity.cash;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("TR_OUTP_FIN_MOP_QUEUE")
public class OutpFinanceMopQueue implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField("schedule_id")
    private String scheduleId;

    @TableField("patient_id")
    private String patientId;

    @TableField("queue_type")
    private String queueType;

    @TableField("is_used")
    private String isUsed;

    @TableField("visit_date")
    private Date visitDate;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;
}
