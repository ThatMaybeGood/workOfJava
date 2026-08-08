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

    /** 数据源名称（唯一标识） */
    private String dsName;

    /** 数据库类型: ORACLE/MYSQL/POSTGRESQL/SQLSERVER（仅JDBC协议时使用） */
    private String dsType;

    /** 连接协议: JDBC / HTTP / SOAP / FILE */
    private String protocol;

    /** 驱动类名（JDBC时使用） */
    private String driverClass;

    /** JDBC URL 或 API 端点 URL */
    private String jdbcUrl;

    /** 用户名（JDBC / Basic Auth） */
    private String username;

    /** 密码（JDBC / Basic Auth，加密存储） */
    private String password;

    /** 连接池参数（仅 JDBC） */
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

    // ── HTTP/SOAP 认证相关字段 ──
    /** 认证类型: NONE / BASIC / TOKEN */
    private String authType;
    /** 认证 Token（Bearer Token） */
    private String authToken;
    /** 默认超时(毫秒) */
    private Integer timeout;
    /** 默认编码 */
    private String encoding;

    /** 是否启用 */
    private String enabled;

    /** 描述 */
    private String description;

    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}

