package com.reports.etl.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("etl_task")
public class EtlTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String extractType;

    /** 关联抽取来源 etl_source.id（Source 改造后优先于旧子表配置） */
    private Long sourceId;

    private Long sourceDsId;

    private Long targetDsId;

    private String targetTable;

    private String writeMode;

    private String queryIndexCols;

    private String updateCols;

    private String cron;

    private Integer enabled;

    private Integer maxRows;

    private Integer batchSize;

    private Integer incremental;

    private String incField;

    private String incPlaceholder;

    private Integer retryCount;

    private String alertConfigJson;

    /** 运行前是否清空目标表（演示任务建议开启，避免主键冲突） */
    private Boolean truncateBeforeWrite;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}