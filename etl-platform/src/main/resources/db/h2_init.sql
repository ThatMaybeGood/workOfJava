-- ============================================
-- ETL通用数据抽取平台 H2元数据库初始化脚本
-- ============================================

-- 1. 数据源配置表
CREATE TABLE datasource_config (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ds_name             VARCHAR(50) NOT NULL UNIQUE,
    ds_type             VARCHAR(20) NOT NULL,
    driver_class        VARCHAR(100) NOT NULL,
    jdbc_url            VARCHAR(500) NOT NULL,
    username            VARCHAR(100) NOT NULL,
    password            VARCHAR(200) NOT NULL,
    initial_size        INT DEFAULT 5,
    min_idle            INT DEFAULT 5,
    max_active          INT DEFAULT 20,
    max_wait            INT DEFAULT 60000,
    validation_query    VARCHAR(100) DEFAULT 'SELECT 1',
    test_on_borrow      CHAR(1) DEFAULT 'Y',
    test_while_idle     CHAR(1) DEFAULT 'Y',
    pool_prepared_statements CHAR(1) DEFAULT 'Y',
    max_pool_prepared_statement_per_connection_size INT DEFAULT 20,
    remove_abandoned    CHAR(1) DEFAULT 'Y',
    remove_abandoned_timeout INT DEFAULT 30,
    connection_timeout  INT DEFAULT 30000,
    enabled             CHAR(1) DEFAULT 'Y',
    description         VARCHAR(200),
    created_time        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time        TIMESTAMP
);

-- 2. ETL任务配置表
CREATE TABLE etl_task_config (
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    task_code               VARCHAR(50) NOT NULL UNIQUE,
    task_name               VARCHAR(100) NOT NULL,
    source_ds_name          VARCHAR(50),
    target_ds_name          VARCHAR(50),
    source_type             VARCHAR(20) DEFAULT 'PROCEDURE',
    source_procedure        VARCHAR(200),
    source_sql              CLOB,
    source_view             VARCHAR(200),
    source_table            VARCHAR(200),
    source_params           VARCHAR(500),
    http_url                VARCHAR(1000),
    http_method             VARCHAR(20) DEFAULT 'GET',
    http_headers            CLOB,
    http_body               CLOB,
    http_auth_type          VARCHAR(20),
    http_username           VARCHAR(100),
    http_password           VARCHAR(200),
    http_token              VARCHAR(500),
    http_response_type      VARCHAR(20) DEFAULT 'JSON',
    http_data_path          VARCHAR(200),
    http_pagination         CHAR(1) DEFAULT 'N',
    http_page_param         VARCHAR(50),
    http_size_param         VARCHAR(50),
    http_page_size          INT DEFAULT 1000,
    http_total_path         VARCHAR(200),
    http_timeout            INT DEFAULT 30000,
    http_encoding           VARCHAR(20) DEFAULT 'UTF-8',
    file_path               VARCHAR(500),
    file_format             VARCHAR(20),
    file_delimiter          VARCHAR(10) DEFAULT ',',
    file_encoding           VARCHAR(20) DEFAULT 'UTF-8',
    file_header             CHAR(1) DEFAULT 'Y',
    file_sheet_name         VARCHAR(100),
    target_table            VARCHAR(100),
    write_mode              VARCHAR(20) DEFAULT 'INSERT',
    truncate_before         CHAR(1) DEFAULT 'N',
    batch_size              INT DEFAULT 2000,
    fetch_size              INT DEFAULT 5000,
    timeout_seconds         INT DEFAULT 1800,
    cron_expr               VARCHAR(50),
    retry_times             INT DEFAULT 0,
    retry_interval          INT DEFAULT 60,
    enabled                 CHAR(1) DEFAULT 'Y',
    priority                INT DEFAULT 5,
    description             VARCHAR(200),
    created_time            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time            TIMESTAMP
);

CREATE INDEX idx_task_code ON etl_task_config(task_code);
CREATE INDEX idx_task_enabled ON etl_task_config(enabled);

-- 3. 字段映射配置表
CREATE TABLE etl_column_mapping (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    task_code           VARCHAR(50) NOT NULL,
    source_column       VARCHAR(100) NOT NULL,
    target_column       VARCHAR(100) NOT NULL,
    data_type           VARCHAR(50) DEFAULT 'VARCHAR',
    default_value       VARCHAR(200),
    transform_expr      VARCHAR(500),
    mapping_order       INT DEFAULT 0,
    is_primary_key      CHAR(1) DEFAULT 'N',
    enabled             CHAR(1) DEFAULT 'Y',
    description         VARCHAR(200)
);

CREATE INDEX idx_mapping_task_code ON etl_column_mapping(task_code);

-- 4. ETL执行日志表
CREATE TABLE etl_execution_log (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    task_code           VARCHAR(50) NOT NULL,
    task_name           VARCHAR(100),
    execution_id        VARCHAR(50) NOT NULL,
    start_time          TIMESTAMP NOT NULL,
    end_time            TIMESTAMP,
    status              VARCHAR(20) NOT NULL,
    total_rows          BIGINT DEFAULT 0,
    success_rows        BIGINT DEFAULT 0,
    failed_rows         BIGINT DEFAULT 0,
    error_message       CLOB,
    error_stack         CLOB,
    execution_duration  BIGINT,
    trigger_type        VARCHAR(20) DEFAULT 'SCHEDULED',
    trigger_user        VARCHAR(50),
    source_info         VARCHAR(200),
    created_time        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_log_task_code ON etl_execution_log(task_code);
CREATE INDEX idx_log_start_time ON etl_execution_log(start_time);
CREATE INDEX idx_log_status ON etl_execution_log(status);

-- 5. ETL任务进度表
CREATE TABLE etl_task_progress (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    task_code           VARCHAR(50) NOT NULL,
    execution_id        VARCHAR(50) NOT NULL,
    total_rows          BIGINT,
    processed_rows      BIGINT DEFAULT 0,
    progress_percent    DECIMAL(5,2) DEFAULT 0,
    last_offset         BIGINT DEFAULT 0,
    status              VARCHAR(20) DEFAULT 'RUNNING',
    last_update_time    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_progress_exec UNIQUE (execution_id)
);
