package com.reports.etl.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * ETL 抽取来源（独立实体）
 * 一个来源可被多个任务复用；类型差异化配置统一存 configJson
 * （WS 类型字段集与 EtlWsConfig 一致，PROC 类型字段集与 EtlProcConfig 一致）
 */
@Data
@TableName("etl_source")
public class EtlSource implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 来源名称 */
    private String name;

    /** 类型：WS / PROC */
    private String type;

    /** 底层数据源ID（PROC 必填；WS 可空） */
    private Long sourceDsId;

    /** 类型差异化配置 JSON */
    private String configJson;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
