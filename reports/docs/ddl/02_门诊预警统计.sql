-- ============================================================
-- 门诊预警统计
-- ============================================================

-- 清理已存在对象(如重建请先执行)

DROP TABLE tr_outp_alt_ov CASCADE CONSTRAINTS;

-- 2.1 门诊预警概览表
CREATE TABLE tr_outp_alt_ov (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    dept_code       VARCHAR2(50),                       -- 科室编码
    dept_name       VARCHAR2(100),                      -- 科室名称
    doctor_name     VARCHAR2(100),                      -- 医生姓名
    clinic_period   VARCHAR2(50),                       -- 出诊时段
    his_logout_time VARCHAR2(20),                       -- HIS退出时间
    remain_alert    NUMBER(10)      DEFAULT 0,          -- 滞留预警
    appointment_alert NUMBER(10)    DEFAULT 0,          -- 预约预警
    early_leave     NUMBER(10)      DEFAULT 0,          -- 早退人数
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 创建索引

CREATE INDEX idx_tr_alert_overview_date ON tr_outp_alt_ov(stat_date);

-- 添加注释

COMMENT ON TABLE tr_outp_alt_ov IS '门诊预警统计-概览';
COMMENT ON TABLE tr_outp_alt_ov IS '门诊预警统计-概览(存储每日门诊预警总览:滞留预警、预约预警、早退人数)';
COMMENT ON COLUMN tr_outp_alt_ov.id IS '主键ID';
COMMENT ON COLUMN tr_outp_alt_ov.stat_date IS '统计日期';
COMMENT ON COLUMN tr_outp_alt_ov.dept_code IS '科室编码';
COMMENT ON COLUMN tr_outp_alt_ov.dept_name IS '科室名称';
COMMENT ON COLUMN tr_outp_alt_ov.doctor_name IS '医生姓名';
COMMENT ON COLUMN tr_outp_alt_ov.clinic_period IS '出诊时段';
COMMENT ON COLUMN tr_outp_alt_ov.his_logout_time IS 'HIS退出时间';
COMMENT ON COLUMN tr_outp_alt_ov.remain_alert IS '滞留预警';
COMMENT ON COLUMN tr_outp_alt_ov.appointment_alert IS '预约预警';
COMMENT ON COLUMN tr_outp_alt_ov.early_leave IS '早退人数';
COMMENT ON COLUMN tr_outp_alt_ov.create_time IS '创建时间';
COMMENT ON COLUMN tr_outp_alt_ov.update_time IS '更新时间';
COMMENT ON COLUMN tr_outp_alt_ov.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_outp_alt_ov.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_outp_alt_ov.ext3 IS '扩展字段3';
