package com.etl.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("etl_pipeline")
public class EtlPipeline {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String pipelineCode;

    private String pipelineName;

    private String cronExpr;

    private Integer retryTimes;

    private Integer retryInterval;

    private String enabled;

    private Integer priority;

    private String description;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}
