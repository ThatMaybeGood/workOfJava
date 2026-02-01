-- 住院现金统计主表
CREATE TABLE mpp_cash_inp_master (
    -- 基础信息
    serial_no VARCHAR2(50) PRIMARY KEY,        -- 流水号/主键
    report_date DATE,                          -- 报表日期
    report_year NUMBER(4),                     -- 报表年份

    -- 状态标志（建议修改为 NUMBER(1)）
    valid_flag NUMBER(1) DEFAULT 1,            -- 是否有效 (0,1 默认1)

    -- 系统审计字段
    creator VARCHAR2(50),                      -- 创建人
    create_time TIMESTAMP DEFAULT SYSTIMESTAMP, -- 创建时间
    update_time TIMESTAMP DEFAULT SYSTIMESTAMP, -- 更新时间

    -- 其他可能需要的业务字段
    report_type VARCHAR2(20) DEFAULT 'DAILY',  -- 报表类型：DAILY-日报，MONTHLY-月报
    report_status VARCHAR2(20) DEFAULT 'DRAFT', -- 报表状态：DRAFT-草稿，SUBMITTED-已提交，APPROVED-已审核
    total_amount NUMBER(18,2),                 -- 总金额（可冗余存储）
    sub_count NUMBER(5) DEFAULT 0              -- 子表数量（冗余字段，方便查询）
);

-- 添加注释
COMMENT ON TABLE mpp_cash_inp_master IS '住院现金统计主表';

COMMENT ON COLUMN mpp_cash_inp_master.serial_no IS '流水号/主键';
COMMENT ON COLUMN mpp_cash_inp_master.report_date IS '报表日期';
COMMENT ON COLUMN mpp_cash_inp_master.report_year IS '报表年份';
COMMENT ON COLUMN mpp_cash_inp_master.valid_flag IS '是否有效：0-无效，1-有效（默认）';
COMMENT ON COLUMN mpp_cash_inp_master.creator IS '创建人';
COMMENT ON COLUMN mpp_cash_inp_master.create_time IS '创建时间';
COMMENT ON COLUMN mpp_cash_inp_master.update_time IS '更新时间';
COMMENT ON COLUMN mpp_cash_inp_master.report_type IS '报表类型：DAILY-日报，MONTHLY-月报';
COMMENT ON COLUMN mpp_cash_inp_master.report_status IS '报表状态：DRAFT-草稿，SUBMITTED-已提交，APPROVED-已审核';
COMMENT ON COLUMN mpp_cash_inp_master.total_amount IS '总金额';
COMMENT ON COLUMN mpp_cash_inp_master.sub_count IS '子表数量';

-- 创建索引
CREATE INDEX idx_mpp_cash_master_report_date ON mpp_cash_inp_master(report_date);
CREATE INDEX idx_mpp_cash_master_valid_flag ON mpp_cash_inp_master(valid_flag);
CREATE UNIQUE INDEX uk_mpp_cash_master_date ON mpp_cash_inp_master(report_date) WHERE valid_flag = 1;

-- 创建触发器自动更新update_time
CREATE OR REPLACE TRIGGER trg_mpp_cash_master_update
BEFORE UPDATE ON mpp_cash_inp_master
FOR EACH ROW
BEGIN
    :NEW.update_time := SYSTIMESTAMP;
END;
/