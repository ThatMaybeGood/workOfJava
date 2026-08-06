package com.etl.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("etl_execution_log")
public class EtlExecutionLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String taskCode;

    private String taskName;

    private String executionId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String status;

    private Long totalRows;

    private Long successRows;

    private Long failedRows;

    private String errorMessage;

    private String errorStack;

    private Long executionDuration;

    private String triggerType;

    private String triggerUser;

    private String sourceInfo;

    private LocalDateTime createdTime;
}
