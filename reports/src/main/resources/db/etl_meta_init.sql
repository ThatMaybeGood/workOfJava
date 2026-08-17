-- =====================================================
-- ETL 元数据库建表脚本（H2，独立于 Oracle 报表库）
-- 幂等执行：IF NOT EXISTS，不会覆盖已有表
-- =====================================================

-- 数据源配置表
CREATE TABLE IF NOT EXISTS etl_datasource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '数据源名称',
    db_type VARCHAR(30) NOT NULL COMMENT '数据库类型：ORACLE/MYSQL/SQLSERVER/POSTGRESQL/DM/H2',
    driver_class VARCHAR(255) COMMENT '驱动类名',
    url VARCHAR(500) NOT NULL COMMENT 'JDBC URL',
    username VARCHAR(100) COMMENT '用户名',
    password VARCHAR(500) COMMENT '加密密码',
    role VARCHAR(20) NOT NULL DEFAULT 'SOURCE' COMMENT '用途：SOURCE=抽取源 / TARGET=目标源',
    pool_initial_size INT DEFAULT 2,
    pool_max_active INT DEFAULT 10,
    enabled SMALLINT DEFAULT 1 COMMENT '1启用 0禁用',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- WebService 任务配置表
CREATE TABLE IF NOT EXISTS etl_task_ws_config (
    id BIGINT PRIMARY KEY,
    task_id BIGINT NOT NULL UNIQUE,
    ws_type VARCHAR(20) NOT NULL DEFAULT 'REST' COMMENT 'SOAP/REST',
    url VARCHAR(1000) COMMENT '接口地址',
    soap_action VARCHAR(500) COMMENT 'SOAPAction',
    request_body_template VARCHAR(4000) COMMENT '请求体模板（JSON/XML占位符）',
    response_path VARCHAR(500) COMMENT '出参定位路径（JSON路径或XML节点）',
    headers_json TEXT COMMENT '自定义请求头 JSON',
    extract_params_json TEXT COMMENT '抽取参数 JSON（分页/增量字段等）',
    max_pages INT DEFAULT 100 COMMENT '最大页码',
    max_rows INT DEFAULT 10000 COMMENT '最大抽取行数保护',
    batch_size INT DEFAULT 500 COMMENT '分批大小'
);

-- 存储过程任务配置表
CREATE TABLE IF NOT EXISTS etl_task_proc_config (
    id BIGINT PRIMARY KEY,
    task_id BIGINT NOT NULL UNIQUE,
    proc_name VARCHAR(200) NOT NULL COMMENT '存储过程名',
    call_template VARCHAR(500) COMMENT '调用模板 CALL proc(?) 或 {call proc(?)}',
    cursor_param_name VARCHAR(100) DEFAULT 'p_cursor' COMMENT '游标出参名',
    cursor_param_idx INT DEFAULT 1 COMMENT '游标参数位置',
    in_params_json TEXT COMMENT 'IN 参数列表 JSON [{name,type,value}]',
    max_pages INT DEFAULT 10,
    max_rows INT DEFAULT 10000,
    batch_size INT DEFAULT 500
);

-- 任务主表
CREATE TABLE IF NOT EXISTS etl_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL COMMENT '任务名称',
    extract_type VARCHAR(30) NOT NULL COMMENT 'WEBSERVICE / PROCEDURE',
    source_ds_id BIGINT COMMENT '抽取源数据源ID',
    target_ds_id BIGINT COMMENT '目标数据源ID',
    target_table VARCHAR(200) NOT NULL COMMENT '目标表名',
    write_mode VARCHAR(20) NOT NULL DEFAULT 'INSERT' COMMENT 'INSERT/UPDATE/UPSERT',
    query_index_cols VARCHAR(500) COMMENT '查询索引列（唯一键）逗号分隔',
    update_cols VARCHAR(500) COMMENT '更新字段（仅 UPDATE/UPSERT）',
    cron VARCHAR(100) COMMENT '定时表达式',
    enabled SMALLINT DEFAULT 0 COMMENT '1启用定时 0停止',
    max_rows INT DEFAULT 10000 COMMENT '最大处理行数保护',
    batch_size INT DEFAULT 500 COMMENT '分批大小',
    incremental SMALLINT DEFAULT 0 COMMENT '0全量 1增量',
    inc_field VARCHAR(100) COMMENT '增量字段名',
    inc_placeholder VARCHAR(100) COMMENT '增量占位符：#lastTime#/#today# 等',
    retry_count INT DEFAULT 0 COMMENT '失败重试次数',
    alert_config_json TEXT COMMENT '告警配置 JSON',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 字段映射表
CREATE TABLE IF NOT EXISTS etl_mapping (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    src_field VARCHAR(300) NOT NULL COMMENT '源字段 dot-path',
    tgt_field VARCHAR(200) NOT NULL COMMENT '目标列名',
    default_value VARCHAR(500) COMMENT '默认值',
    is_update_col SMALLINT DEFAULT 0 COMMENT '是否作为更新字段',
    src_type VARCHAR(50) COMMENT '源字段类型',
    tgt_type VARCHAR(50) COMMENT '目标字段类型',
    sort_order INT DEFAULT 0 COMMENT '映射顺序',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 任务执行历史表
CREATE TABLE IF NOT EXISTS etl_task_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    task_name VARCHAR(200),
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    status VARCHAR(20) COMMENT 'SUCCESS/FAILED/CANCELLED',
    extracted_rows INT DEFAULT 0 COMMENT '读取行数',
    written_rows INT DEFAULT 0 COMMENT '写入行数',
    error_msg VARCHAR(2000) COMMENT '错误信息',
    trigger_type VARCHAR(20) COMMENT 'MANUAL/SCHEDULED',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 步骤日志表（每个环节一条）
CREATE TABLE IF NOT EXISTS etl_step_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    log_id BIGINT NOT NULL COMMENT '关联 etl_task_log.id',
    step_name VARCHAR(30) NOT NULL COMMENT 'EXTRACT/TRANSFORM/LOAD',
    status VARCHAR(20) COMMENT 'RUNNING/SUCCESS/FAILED',
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    duration_ms BIGINT,
    rows_count INT DEFAULT 0,
    detail VARCHAR(2000) COMMENT '详情/错误摘要',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 日志保留策略表
CREATE TABLE IF NOT EXISTS etl_log_config (
    id BIGINT PRIMARY KEY DEFAULT 1,
    save_days INT DEFAULT 30 COMMENT '日志保留天数',
    auto_clean SMALLINT DEFAULT 1 COMMENT '是否自动清理',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 告警配置表
CREATE TABLE IF NOT EXISTS etl_alert_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    channel VARCHAR(30) COMMENT 'mail/dingtalk/webhook',
    receivers VARCHAR(500) COMMENT '接收人地址/URL',
    threshold INT DEFAULT 1 COMMENT '连续失败 N 次后通知',
    enabled SMALLINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 初始化保留策略
INSERT INTO etl_log_config(id, save_days, auto_clean)
VALUES (1, 30, 1)
ON CONFLICT(id) DO NOTHING;
