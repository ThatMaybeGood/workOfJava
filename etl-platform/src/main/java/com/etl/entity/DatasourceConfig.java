package com.etl.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("datasource_config")
public class DatasourceConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String dsName;

    private String dsType;

    private String driverClass;

    private String jdbcUrl;

    private String username;

    private String password;

    private Integer initialSize;

    private Integer minIdle;

    private Integer maxActive;

    private Long maxWait;

    private String validationQuery;

    private String testOnBorrow;

    private String testWhileIdle;

    private String poolPreparedStatements;

    private Integer maxPoolPreparedStatementPerConnectionSize;

    private String removeAbandoned;

    private Integer removeAbandonedTimeout;

    private Long connectionTimeout;

    private String enabled;

    private String description;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}
