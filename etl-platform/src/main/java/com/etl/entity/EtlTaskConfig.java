package com.etl.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("etl_task_config")
public class EtlTaskConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String taskCode;

    private String taskName;

    private String sourceDsName;

    private String targetDsName;

    private String sourceType;

    private String sourceProcedure;

    private String sourceSql;

    private String sourceView;

    private String sourceTable;

    private String sourceParams;

    // HTTP related fields
    private String httpUrl;
    private String httpMethod;
    private String httpHeaders;
    private String httpBody;
    private String httpAuthType;
    private String httpUsername;
    private String httpPassword;
    private String httpToken;
    private String httpResponseType;
    private String httpDataPath;
    private String httpPagination;
    private String httpPageParam;
    private String httpSizeParam;
    private Integer httpPageSize;
    private String httpTotalPath;
    private Integer httpTimeout;
    private String httpEncoding;

    // File related fields
    private String filePath;
    private String fileFormat;
    private String fileDelimiter;
    private String fileEncoding;
    private String fileHeader;
    private String fileSheetName;

    private String targetTable;

    private String writeMode;

    private String truncateBefore;

    private Integer batchSize;

    private Integer fetchSize;

    private Integer timeoutSeconds;

    private String cronExpr;

    private Integer retryTimes;

    private Integer retryInterval;

    private String enabled;

    private Integer priority;

    private String description;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}
