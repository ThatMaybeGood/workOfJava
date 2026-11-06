-- 住院现金统计主表（与 InpCashMainEntity 代码一致）
DROP TABLE mpp_cash_inp_master;

CREATE TABLE mpp_cash_inp_master (
    serial_no VARCHAR2(50) PRIMARY KEY,         -- 流水号/主键
    report_date DATE,                           -- 报表日期
    report_year NUMBER(4),                      -- 报表年份
    valid_flag NUMBER(1) DEFAULT 1,             -- 是否有效：0-无效，1-有效
    creator VARCHAR2(50),                       -- 创建人
    create_time TIMESTAMP DEFAULT SYSTIMESTAMP, -- 创建时间
    update_time TIMESTAMP DEFAULT SYSTIMESTAMP, -- 更新时间
    holiday_total_flag VARCHAR2(1) DEFAULT '0', -- 节假日汇总标志：0-正常，1-汇总
    total_remark VARCHAR2(500),                 -- 报表级备注（合计行下方整表一条）
    audit_status VARCHAR2(1) DEFAULT '0',       -- 审核状态：0-未审核，1-审核通过，2-审核不通过
    audit_by VARCHAR2(50),                      -- 审核人
    audit_time TIMESTAMP,                       -- 审核时间
    audit_remark VARCHAR2(500),                 -- 审核意见（不通过原因等）
    holiday_start_date DATE,                    -- 节假日汇总区间起（跨月分段汇总时区分各段）
    holiday_end_date DATE                       -- 节假日汇总区间止
);

COMMENT ON TABLE mpp_cash_inp_master IS '住院现金统计主表';
COMMENT ON COLUMN mpp_cash_inp_master.serial_no IS '流水号/主键';
COMMENT ON COLUMN mpp_cash_inp_master.report_date IS '报表日期';
COMMENT ON COLUMN mpp_cash_inp_master.report_year IS '报表年份';
COMMENT ON COLUMN mpp_cash_inp_master.valid_flag IS '是否有效：0-无效，1-有效';
COMMENT ON COLUMN mpp_cash_inp_master.creator IS '创建人';
COMMENT ON COLUMN mpp_cash_inp_master.create_time IS '创建时间';
COMMENT ON COLUMN mpp_cash_inp_master.update_time IS '更新时间';
COMMENT ON COLUMN mpp_cash_inp_master.holiday_total_flag IS '节假日汇总标志：0-正常，1-汇总';
COMMENT ON COLUMN mpp_cash_inp_master.total_remark IS '报表级备注（合计行下方整表一条）';
COMMENT ON COLUMN mpp_cash_inp_master.audit_status IS '审核状态：0-未审核，1-审核通过，2-审核不通过';
COMMENT ON COLUMN mpp_cash_inp_master.audit_by IS '审核人';
COMMENT ON COLUMN mpp_cash_inp_master.audit_time IS '审核时间';
COMMENT ON COLUMN mpp_cash_inp_master.audit_remark IS '审核意见（不通过原因等）';
COMMENT ON COLUMN mpp_cash_inp_master.holiday_start_date IS '节假日汇总区间起（跨月分段汇总时区分各段）';
COMMENT ON COLUMN mpp_cash_inp_master.holiday_end_date IS '节假日汇总区间止';

CREATE INDEX idx_mpp_cash_master_rpt_date ON mpp_cash_inp_master(report_date);
CREATE INDEX idx_mpp_cash_master_valid_flag ON mpp_cash_inp_master(valid_flag);

CREATE OR REPLACE TRIGGER trg_mpp_cash_master_update
BEFORE UPDATE ON mpp_cash_inp_master
FOR EACH ROW
BEGIN
    :NEW.update_time := SYSTIMESTAMP;
END;
/

-- ==================== 2026-11-06 版本1: 生成来源列 ====================
-- 背景: 审核界面查询时若该日期报表未生成，允许自动补生成，需记录报表生成来源
-- 取值: 1-报表界面生成(默认) 2-审核界面补生成
DECLARE
    v_cnt NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_cnt FROM user_tab_columns
    WHERE table_name = 'MPP_CASH_INP_MASTER' AND column_name = 'GEN_SOURCE';
    IF v_cnt = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE mpp_cash_inp_master ADD (gen_source VARCHAR2(1) DEFAULT ''1'')';
    END IF;
END;
/

COMMENT ON COLUMN mpp_cash_inp_master.gen_source IS '生成来源：1-报表界面，2-审核界面补生成';

-- 验证: SELECT column_name FROM user_tab_columns WHERE table_name='MPP_CASH_INP_MASTER' AND column_name='GEN_SOURCE';
-- ==================== 2026-11-06 版本1 结束 ====================
