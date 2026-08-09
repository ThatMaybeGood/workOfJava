package com.etl.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("etl_pipeline_edge")
public class EtlPipelineEdge {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long pipelineId;

    private Long fromStepId;

    private Long toStepId;

    /** PASS / JOIN / UNION */
    private String edgeType;

    /** 连线配置 JSON（Join 条件 / Union 规则等） */
    private String edgeConfig;
}
