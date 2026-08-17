package com.reports.etl.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("etl_step_log")
public class EtlStepLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long logId;

    private String stepName;

    private String status;

    private Date startTime;

    private Date endTime;

    private Long durationMs;

    private Integer rowsCount;

    private String detail;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}