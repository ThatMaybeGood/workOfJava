package com.etl.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("etl_pipeline_step")
public class EtlPipelineStep {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long pipelineId;

    private String stepCode;

    private String stepName;

    /** EXTRACT / TRANSFORM / LOAD */
    private String stepType;

    /** 子类型: FIELD_MAP / JOIN / UNION (TRANSFORM), DB_INSERT / FILE_CSV / FILE_JSON / FILE_EXCEL (LOAD) */
    private String stepSubType;

    private Integer orderIndex;

    /** 抽取：源数据源名称 */
    private String sourceDsName;

    /** 抽取：源类型 SQL/TABLE/VIEW/PROCEDURE/HTTP/SOAP/FILE */
    private String sourceType;

    /** 抽取：详细配置 JSON */
    private String sourceConfig;

    /** 加载：目标数据源名称 */
    private String targetDsName;

    /** 加载：详细配置 JSON */
    private String targetConfig;

    /** 加载：写入模式 INSERT/MERGE */
    private String writeMode;

    private Integer batchSize;

    private Integer timeoutSeconds;

    private String enabled;

    private String description;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}
