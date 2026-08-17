package com.reports.etl.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("etl_task_ws_config")
public class EtlWsConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long taskId;

    private String wsType;

    private String url;

    private String soapAction;

    private String requestBodyTemplate;

    private String responsePath;

    private String headersJson;

    private String extractParamsJson;

    private Integer maxPages;

    private Integer maxRows;

    private Integer batchSize;
}