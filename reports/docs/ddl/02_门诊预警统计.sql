-- ============================================================
-- 门诊预警统计
-- ============================================================

-- 清理已存在对象(如重建请先执行)

DROP TABLE tr_outp_alt_ov CASCADE CONSTRAINTS;

DROP TABLE tr_outp_alt_dept CASCADE CONSTRAINTS;

DROP TABLE tr_outp_alt_doc CASCADE CONSTRAINTS;

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

-- 2.2 门诊预警科室明细表
CREATE TABLE tr_outp_alt_dept (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    dept_name       VARCHAR2(100)   NOT NULL,           -- 科室名称
    remain_alert    NUMBER(10)      DEFAULT 0,          -- 滞留预警
    appointment_alert NUMBER(10)    DEFAULT 0,          -- 预约预警
    early_leave     NUMBER(10)      DEFAULT 0,          -- 早退人数
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 2.3 门诊预警医生明细表
CREATE TABLE tr_outp_alt_doc (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    doctor_name     VARCHAR2(100)   NOT NULL,           -- 医生姓名
    dept_name       VARCHAR2(100),                      -- 科室名称
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
CREATE INDEX idx_tr_alert_dept_date ON tr_outp_alt_dept(stat_date);
CREATE INDEX idx_tr_alert_dept_name ON tr_outp_alt_dept(dept_name);
CREATE INDEX idx_tr_alert_doctor_date ON tr_outp_alt_doc(stat_date);
CREATE INDEX idx_tr_alert_doctor_name ON tr_outp_alt_doc(doctor_name);

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
COMMENT ON TABLE tr_outp_alt_dept IS '门诊预警统计-科室明细';
COMMENT ON TABLE tr_outp_alt_dept IS '门诊预警统计-科室明细(按科室维度统计滞留预警、预约预警、早退人数)';
COMMENT ON COLUMN tr_outp_alt_dept.id IS '主键ID';
COMMENT ON COLUMN tr_outp_alt_dept.stat_date IS '统计日期';
COMMENT ON COLUMN tr_outp_alt_dept.dept_name IS '科室名称';
COMMENT ON COLUMN tr_outp_alt_dept.remain_alert IS '滞留预警';
COMMENT ON COLUMN tr_outp_alt_dept.appointment_alert IS '预约预警';
COMMENT ON COLUMN tr_outp_alt_dept.early_leave IS '早退人数';
COMMENT ON COLUMN tr_outp_alt_dept.create_time IS '创建时间';
COMMENT ON COLUMN tr_outp_alt_dept.update_time IS '更新时间';
COMMENT ON COLUMN tr_outp_alt_dept.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_outp_alt_dept.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_outp_alt_dept.ext3 IS '扩展字段3';
COMMENT ON TABLE tr_outp_alt_doc IS '门诊预警统计-医生明细';
COMMENT ON TABLE tr_outp_alt_doc IS '门诊预警统计-医生明细(按医生维度统计滞留预警、预约预警、早退人数)';
COMMENT ON COLUMN tr_outp_alt_doc.id IS '主键ID';
COMMENT ON COLUMN tr_outp_alt_doc.stat_date IS '统计日期';
COMMENT ON COLUMN tr_outp_alt_doc.doctor_name IS '医生姓名';
COMMENT ON COLUMN tr_outp_alt_doc.dept_name IS '科室名称';
COMMENT ON COLUMN tr_outp_alt_doc.remain_alert IS '滞留预警';
COMMENT ON COLUMN tr_outp_alt_doc.appointment_alert IS '预约预警';
COMMENT ON COLUMN tr_outp_alt_doc.early_leave IS '早退人数';
COMMENT ON COLUMN tr_outp_alt_doc.create_time IS '创建时间';
COMMENT ON COLUMN tr_outp_alt_doc.update_time IS '更新时间';
COMMENT ON COLUMN tr_outp_alt_doc.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_outp_alt_doc.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_outp_alt_doc.ext3 IS '扩展字段3';
