package com.etl.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("etl_task_progress")
public class EtlTaskProgress {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String taskCode;

    private String executionId;

    private Long totalRows;

    private Long processedRows;

    private Double progressPercent;

    private Long lastOffset;

    private String status;

    private LocalDateTime lastUpdateTime;
}
