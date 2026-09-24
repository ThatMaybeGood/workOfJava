-- 住院现金统计审核日志表（每次审核动作记一条，历史记录可追溯）
DROP TABLE mpp_cash_inp_audit_log;

CREATE TABLE mpp_cash_inp_audit_log (
    serial_no VARCHAR2(50) PRIMARY KEY,         -- 日志流水号
    report_date DATE,                           -- 报表日期
    holiday_total_flag VARCHAR2(1) DEFAULT '0', -- 节假日汇总标志：0-正常，1-汇总
    audit_status VARCHAR2(1),                   -- 审核动作：1-审核通过，2-审核不通过，0-取消审核
    audit_by VARCHAR2(50),                      -- 审核人
    audit_time TIMESTAMP DEFAULT SYSTIMESTAMP,  -- 审核时间
    audit_remark VARCHAR2(500)                  -- 审核意见
);

COMMENT ON TABLE mpp_cash_inp_audit_log IS '住院现金统计审核日志表';
COMMENT ON COLUMN mpp_cash_inp_audit_log.serial_no IS '日志流水号';
COMMENT ON COLUMN mpp_cash_inp_audit_log.report_date IS '报表日期';
COMMENT ON COLUMN mpp_cash_inp_audit_log.holiday_total_flag IS '节假日汇总标志：0-正常，1-汇总';
COMMENT ON COLUMN mpp_cash_inp_audit_log.audit_status IS '审核动作：1-审核通过，2-审核不通过，0-取消审核';
COMMENT ON COLUMN mpp_cash_inp_audit_log.audit_by IS '审核人';
COMMENT ON COLUMN mpp_cash_inp_audit_log.audit_time IS '审核时间';
COMMENT ON COLUMN mpp_cash_inp_audit_log.audit_remark IS '审核意见';

CREATE INDEX idx_mpp_cash_inp_audit_log_date ON mpp_cash_inp_audit_log(report_date, holiday_total_flag);
