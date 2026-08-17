package com.reports.etl.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("etl_task_proc_config")
public class EtlProcConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long taskId;

    private String procName;

    private String callTemplate;

    private String cursorParamName;

    private Integer cursorParamIdx;

    private String inParamsJson;

    private Integer maxPages;

    private Integer maxRows;

    private Integer batchSize;
}