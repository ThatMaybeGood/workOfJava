-- ============================================================
-- 医技统计
-- ============================================================

-- 清理已存在对象(如重建请先执行)

DROP TABLE tr_medtech_ov CASCADE CONSTRAINTS;

DROP TABLE tr_medtech_dtl CASCADE CONSTRAINTS;

-- 6.1 医技统计概览表
CREATE TABLE tr_medtech_ov (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    check_count     NUMBER(10)      DEFAULT 0,          -- 检查人次
    on_time_rate    VARCHAR2(20),                      -- 准时率
    wait_time       VARCHAR2(20),                      -- 等候时长
    avg_wait_late   VARCHAR2(20),                      -- 平均迟到
    avg_report_time VARCHAR2(20),                      -- 平均报告时长
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 6.2 医技统计科室明细表
CREATE TABLE tr_medtech_dtl (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    dept_name       VARCHAR2(100)   NOT NULL,           -- 科室名称
    check_count     NUMBER(10)      DEFAULT 0,          -- 检查人次
    on_time_rate    VARCHAR2(20),                      -- 准时率
    wait_time       NUMBER(10,2),                       -- 等候时长
    avg_wait_late   NUMBER(10,2),                       -- 平均迟到
    avg_report_time NUMBER(10,2),                       -- 平均报告时长
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 创建索引

CREATE INDEX idx_tr_med_overview_date ON tr_medtech_ov(stat_date);
CREATE INDEX idx_tr_med_detail_dept ON tr_medtech_dtl(dept_name);

-- 添加注释

COMMENT ON TABLE tr_medtech_ov IS '医技统计-概览';
COMMENT ON TABLE tr_medtech_ov IS '医技统计-概览(医技科室运营总览指标:检查人次、准时率、等候时长、报告时长)';
COMMENT ON COLUMN tr_medtech_ov.id IS '主键ID';
COMMENT ON COLUMN tr_medtech_ov.stat_date IS '统计日期';
COMMENT ON COLUMN tr_medtech_ov.check_count IS '检查人次';
COMMENT ON COLUMN tr_medtech_ov.on_time_rate IS '准时率';
COMMENT ON COLUMN tr_medtech_ov.wait_time IS '等候时长';
COMMENT ON COLUMN tr_medtech_ov.avg_wait_late IS '平均迟到';
COMMENT ON COLUMN tr_medtech_ov.avg_report_time IS '平均报告时长';
COMMENT ON COLUMN tr_medtech_ov.create_time IS '创建时间';
COMMENT ON COLUMN tr_medtech_ov.update_time IS '更新时间';
COMMENT ON COLUMN tr_medtech_ov.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_medtech_ov.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_medtech_ov.ext3 IS '扩展字段3';
COMMENT ON TABLE tr_medtech_dtl IS '医技统计-科室明细';
COMMENT ON TABLE tr_medtech_dtl IS '医技统计-科室明细(按医技科室维度统计检查人次、准时率、等候时长等)';
COMMENT ON COLUMN tr_medtech_dtl.id IS '主键ID';
COMMENT ON COLUMN tr_medtech_dtl.stat_date IS '统计日期';
COMMENT ON COLUMN tr_medtech_dtl.dept_name IS '科室名称';
COMMENT ON COLUMN tr_medtech_dtl.check_count IS '检查人次';
COMMENT ON COLUMN tr_medtech_dtl.on_time_rate IS '准时率';
COMMENT ON COLUMN tr_medtech_dtl.wait_time IS '等候时长';
COMMENT ON COLUMN tr_medtech_dtl.avg_wait_late IS '平均迟到';
COMMENT ON COLUMN tr_medtech_dtl.avg_report_time IS '平均报告时长';
COMMENT ON COLUMN tr_medtech_dtl.create_time IS '创建时间';
COMMENT ON COLUMN tr_medtech_dtl.update_time IS '更新时间';
COMMENT ON COLUMN tr_medtech_dtl.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_medtech_dtl.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_medtech_dtl.ext3 IS '扩展字段3';
