package com.reports.etl.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("etl_task_log")
public class EtlTaskLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private String taskName;

    private Date startTime;

    private Date endTime;

    private String status;

    private Integer extractedRows;

    private Integer writtenRows;

    private String errorMsg;

    private String triggerType;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}