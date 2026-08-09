-- ============================================
-- ETL通用数据抽取平台 H2元数据库初始化脚本
-- ============================================

-- 1. 数据源配置表
CREATE TABLE IF NOT EXISTS datasource_config (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ds_name             VARCHAR(50) NOT NULL UNIQUE,
    ds_type             VARCHAR(20) NOT NULL,
    protocol            VARCHAR(20) DEFAULT 'JDBC',
    driver_class        VARCHAR(100) NOT NULL,
    jdbc_url            VARCHAR(500) NOT NULL,
    username            VARCHAR(100) DEFAULT '',
    password            VARCHAR(200) DEFAULT '',
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
    auth_type           VARCHAR(20),
    auth_token          VARCHAR(500),
    timeout             INT DEFAULT 30000,
    encoding            VARCHAR(20) DEFAULT 'UTF-8',
    enabled             CHAR(1) DEFAULT 'Y',
    description         VARCHAR(200),
    created_time        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time        TIMESTAMP
);

-- 2. ETL任务配置表
CREATE TABLE IF NOT EXISTS etl_task_config (
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
    soap_action             VARCHAR(500),
    soap_binding            VARCHAR(20),
    soap_namespace          VARCHAR(200),
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

CREATE INDEX IF NOT EXISTS idx_task_code ON etl_task_config(task_code);
CREATE INDEX IF NOT EXISTS idx_task_enabled ON etl_task_config(enabled);

-- 3. 字段映射配置表
CREATE TABLE IF NOT EXISTS etl_column_mapping (
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

CREATE INDEX IF NOT EXISTS idx_mapping_task_code ON etl_column_mapping(task_code);

-- 4. ETL执行日志表
CREATE TABLE IF NOT EXISTS etl_execution_log (
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

CREATE INDEX IF NOT EXISTS idx_log_task_code ON etl_execution_log(task_code);
CREATE INDEX IF NOT EXISTS idx_log_start_time ON etl_execution_log(start_time);
CREATE INDEX IF NOT EXISTS idx_log_status ON etl_execution_log(status);

-- 5. ETL任务进度表
CREATE TABLE IF NOT EXISTS etl_task_progress (
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

-- ============================================
-- 以下为 Pipeline 模式新表（v2 架构）
-- ============================================

-- 6. 管线定义表
CREATE TABLE IF NOT EXISTS etl_pipeline (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pipeline_code   VARCHAR(50) NOT NULL UNIQUE,
    pipeline_name   VARCHAR(100) NOT NULL,
    cron_expr       VARCHAR(50),
    retry_times     INT DEFAULT 0,
    retry_interval  INT DEFAULT 60,
    enabled         CHAR(1) DEFAULT 'Y',
    priority        INT DEFAULT 5,
    description     VARCHAR(200),
    created_time    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time    TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_pipeline_code ON etl_pipeline(pipeline_code);
CREATE INDEX IF NOT EXISTS idx_pipeline_enabled ON etl_pipeline(enabled);

-- 7. 管线步骤表
CREATE TABLE IF NOT EXISTS etl_pipeline_step (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pipeline_id     BIGINT NOT NULL,
    step_code       VARCHAR(50) NOT NULL,
    step_name       VARCHAR(100) NOT NULL,
    step_type       VARCHAR(20) NOT NULL,
    step_sub_type   VARCHAR(30),
    order_index     INT DEFAULT 0,
    source_ds_name  VARCHAR(50),
    source_type     VARCHAR(20),
    source_config   CLOB,
    target_ds_name  VARCHAR(50),
    target_config   CLOB,
    write_mode      VARCHAR(20),
    batch_size      INT DEFAULT 2000,
    timeout_seconds INT DEFAULT 1800,
    enabled         CHAR(1) DEFAULT 'Y',
    description     VARCHAR(200),
    created_time    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time    TIMESTAMP,
    FOREIGN KEY (pipeline_id) REFERENCES etl_pipeline(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_step_pipeline ON etl_pipeline_step(pipeline_id);

-- 8. 管线连线表
CREATE TABLE IF NOT EXISTS etl_pipeline_edge (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pipeline_id     BIGINT NOT NULL,
    from_step_id    BIGINT NOT NULL,
    to_step_id      BIGINT NOT NULL,
    edge_type       VARCHAR(20) DEFAULT 'PASS',
    edge_config     CLOB,
    FOREIGN KEY (pipeline_id) REFERENCES etl_pipeline(id) ON DELETE CASCADE,
    FOREIGN KEY (from_step_id) REFERENCES etl_pipeline_step(id) ON DELETE CASCADE,
    FOREIGN KEY (to_step_id) REFERENCES etl_pipeline_step(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_edge_pipeline ON etl_pipeline_edge(pipeline_id);

-- 9. 字段映射表（v2：关联到步骤而非任务编码）
CREATE TABLE IF NOT EXISTS etl_step_column_mapping (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    step_id         BIGINT NOT NULL,
    source_column   VARCHAR(100) NOT NULL,
    target_column   VARCHAR(100) NOT NULL,
    data_type       VARCHAR(50) DEFAULT 'VARCHAR',
    default_value   VARCHAR(200),
    transform_expr  VARCHAR(500),
    mapping_order   INT DEFAULT 0,
    is_primary_key  CHAR(1) DEFAULT 'N',
    enabled         CHAR(1) DEFAULT 'Y',
    description     VARCHAR(200),
    FOREIGN KEY (step_id) REFERENCES etl_pipeline_step(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_step_mapping ON etl_step_column_mapping(step_id);
