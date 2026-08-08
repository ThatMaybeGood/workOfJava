-- ============================================
-- ETL通用数据抽取平台 数据库初始化脚本
-- Oracle版本
-- ============================================

-- 1. 数据源配置表
CREATE TABLE datasource_config (
    id                  NUMBER PRIMARY KEY,
    ds_name             VARCHAR2(50) NOT NULL UNIQUE,
    ds_type             VARCHAR2(20) NOT NULL,          -- ORACLE/MYSQL/POSTGRESQL/SQLSERVER/HTTP/SOAP/FILE
    protocol            VARCHAR2(20) DEFAULT 'JDBC',     -- JDBC/HTTP/SOAP/FILE
    driver_class        VARCHAR2(100) NOT NULL,
    jdbc_url            VARCHAR2(500) NOT NULL,
    username            VARCHAR2(100) NOT NULL,
    password            VARCHAR2(200) NOT NULL,         -- AES加密存储
    initial_size        NUMBER DEFAULT 5,
    min_idle            NUMBER DEFAULT 5,
    max_active          NUMBER DEFAULT 20,
    max_wait            NUMBER DEFAULT 60000,
    validation_query    VARCHAR2(100) DEFAULT 'SELECT 1 FROM DUAL',
    test_on_borrow      CHAR(1) DEFAULT 'Y',
    test_while_idle     CHAR(1) DEFAULT 'Y',
    pool_prepared_statements CHAR(1) DEFAULT 'Y',
    max_pool_prepared_statement_per_connection_size NUMBER DEFAULT 20,
    remove_abandoned    CHAR(1) DEFAULT 'Y',
    remove_abandoned_timeout NUMBER DEFAULT 30,
    connection_timeout  NUMBER DEFAULT 30000,
    auth_type           VARCHAR2(20),
    auth_token          VARCHAR2(500),
    timeout             NUMBER DEFAULT 30000,
    encoding            VARCHAR2(20) DEFAULT 'UTF-8',
    enabled             CHAR(1) DEFAULT 'Y',
    description         VARCHAR2(200),
    created_time        TIMESTAMP DEFAULT SYSTIMESTAMP,
    updated_time        TIMESTAMP
);

COMMENT ON TABLE datasource_config IS '数据源配置表';
COMMENT ON COLUMN datasource_config.ds_name IS '数据源名称，唯一标识';
COMMENT ON COLUMN datasource_config.ds_type IS '数据库类型：ORACLE/MYSQL/POSTGRESQL/SQLSERVER 或 HTTP/SOAP/FILE';
COMMENT ON COLUMN datasource_config.protocol IS '连接协议：JDBC/HTTP/SOAP/FILE';
COMMENT ON COLUMN datasource_config.password IS '密码使用AES加密存储';

-- 2. ETL任务配置表
CREATE TABLE etl_task_config (
    id                      NUMBER PRIMARY KEY,
    task_code               VARCHAR2(50) NOT NULL UNIQUE,
    task_name               VARCHAR2(100) NOT NULL,
    source_ds_name          VARCHAR2(50) NOT NULL,
    target_ds_name          VARCHAR2(50) NOT NULL,
    source_type             VARCHAR2(20) DEFAULT 'PROCEDURE', -- PROCEDURE/SQL/VIEW/TABLE/HTTP/SOAP/FILE
    source_procedure        VARCHAR2(200),
    source_sql              CLOB,
    source_view             VARCHAR2(200),
    source_table            VARCHAR2(200),
    source_params           VARCHAR2(500),
    http_url                VARCHAR2(1000),
    http_method             VARCHAR2(20) DEFAULT 'GET',
    http_headers            CLOB,
    http_body               CLOB,
    http_auth_type          VARCHAR2(20),
    http_username           VARCHAR2(100),
    http_password           VARCHAR2(200),
    http_token              VARCHAR2(500),
    http_response_type      VARCHAR2(20) DEFAULT 'JSON',
    http_data_path          VARCHAR2(200),
    http_pagination         CHAR(1) DEFAULT 'N',
    http_page_param         VARCHAR2(50),
    http_size_param         VARCHAR2(50),
    http_page_size          NUMBER DEFAULT 1000,
    http_total_path         VARCHAR2(200),
    http_timeout            NUMBER DEFAULT 30000,
    http_encoding           VARCHAR2(20) DEFAULT 'UTF-8',
    soap_action             VARCHAR2(500),
    soap_binding            VARCHAR2(20),
    soap_namespace          VARCHAR2(200),
    file_path               VARCHAR2(500),
    file_format             VARCHAR2(20),
    file_delimiter          VARCHAR2(10) DEFAULT ',',
    file_encoding           VARCHAR2(20) DEFAULT 'UTF-8',
    file_header             CHAR(1) DEFAULT 'Y',
    file_sheet_name         VARCHAR2(100),
    target_table            VARCHAR2(100) NOT NULL,
    write_mode              VARCHAR2(20) DEFAULT 'INSERT',
    truncate_before         CHAR(1) DEFAULT 'N',
    batch_size              NUMBER DEFAULT 2000,
    fetch_size              NUMBER DEFAULT 5000,
    timeout_seconds         NUMBER DEFAULT 1800,
    cron_expr               VARCHAR2(50) NOT NULL,
    retry_times             NUMBER DEFAULT 0,
    retry_interval          NUMBER DEFAULT 60,
    enabled                 CHAR(1) DEFAULT 'Y',
    priority                NUMBER DEFAULT 5,
    description             VARCHAR2(200),
    created_time            TIMESTAMP DEFAULT SYSTIMESTAMP,
    updated_time            TIMESTAMP,
    CONSTRAINT fk_task_source FOREIGN KEY (source_ds_name) REFERENCES datasource_config(ds_name),
    CONSTRAINT fk_task_target FOREIGN KEY (target_ds_name) REFERENCES datasource_config(ds_name)
);

CREATE INDEX idx_task_code ON etl_task_config(task_code);
CREATE INDEX idx_task_enabled ON etl_task_config(enabled);

COMMENT ON TABLE etl_task_config IS 'ETL任务配置表';
COMMENT ON COLUMN etl_task_config.source_type IS '抽取方式：PROCEDURE/SQL/VIEW/TABLE/HTTP/SOAP/FILE';
COMMENT ON COLUMN etl_task_config.write_mode IS '写入模式：INSERT/MERGE';

-- 3. 字段映射配置表
CREATE TABLE etl_column_mapping (
    id                  NUMBER PRIMARY KEY,
    task_code           VARCHAR2(50) NOT NULL,
    source_column       VARCHAR2(100) NOT NULL,
    target_column       VARCHAR2(100) NOT NULL,
    data_type           VARCHAR2(50) DEFAULT 'VARCHAR2',
    default_value       VARCHAR2(200),
    transform_expr      VARCHAR2(500),
    mapping_order       NUMBER DEFAULT 0,
    is_primary_key      CHAR(1) DEFAULT 'N',
    enabled             CHAR(1) DEFAULT 'Y',
    description         VARCHAR2(200),
    CONSTRAINT fk_mapping_task FOREIGN KEY (task_code) REFERENCES etl_task_config(task_code) ON DELETE CASCADE
);

COMMENT ON TABLE etl_column_mapping IS '字段映射配置表';
COMMENT ON COLUMN etl_column_mapping.is_primary_key IS '是否为主键，MERGE模式用于匹配条件';

-- 4. ETL执行日志表
CREATE TABLE etl_execution_log (
    id                  NUMBER PRIMARY KEY,
    task_code           VARCHAR2(50) NOT NULL,
    task_name           VARCHAR2(100),
    execution_id        VARCHAR2(50) NOT NULL,
    start_time          TIMESTAMP NOT NULL,
    end_time            TIMESTAMP,
    status              VARCHAR2(20) NOT NULL,          -- RUNNING/SUCCESS/FAILED/TIMEOUT
    total_rows          NUMBER DEFAULT 0,
    success_rows        NUMBER DEFAULT 0,
    failed_rows         NUMBER DEFAULT 0,
    error_message       CLOB,
    error_stack         CLOB,
    execution_duration  NUMBER,
    trigger_type        VARCHAR2(20) DEFAULT 'SCHEDULED',
    trigger_user        VARCHAR2(50),
    source_info         VARCHAR2(200),
    created_time        TIMESTAMP DEFAULT SYSTIMESTAMP,
    CONSTRAINT fk_log_task FOREIGN KEY (task_code) REFERENCES etl_task_config(task_code)
);

CREATE INDEX idx_log_task_code ON etl_execution_log(task_code);
CREATE INDEX idx_log_start_time ON etl_execution_log(start_time);
CREATE INDEX idx_log_status ON etl_execution_log(status);

COMMENT ON TABLE etl_execution_log IS 'ETL执行日志表';

-- 5. ETL任务进度表
CREATE TABLE etl_task_progress (
    id                  NUMBER PRIMARY KEY,
    task_code           VARCHAR2(50) NOT NULL,
    execution_id        VARCHAR2(50) NOT NULL,
    total_rows          NUMBER,
    processed_rows      NUMBER DEFAULT 0,
    progress_percent    NUMBER(5,2) DEFAULT 0,
    last_offset         NUMBER DEFAULT 0,
    status              VARCHAR2(20) DEFAULT 'RUNNING',
    last_update_time    TIMESTAMP DEFAULT SYSTIMESTAMP,
    CONSTRAINT fk_progress_task FOREIGN KEY (task_code) REFERENCES etl_task_config(task_code),
    CONSTRAINT uk_progress_exec UNIQUE (execution_id)
);

COMMENT ON TABLE etl_task_progress IS 'ETL任务进度表';
