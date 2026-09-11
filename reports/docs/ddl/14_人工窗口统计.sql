-- ============================================================
-- 人工窗口统计
-- ============================================================

-- 清理已存在对象(如重建请先执行)

DROP TABLE tr_win_stat_ov CASCADE CONSTRAINTS;

DROP TABLE tr_win_stat_age CASCADE CONSTRAINTS;

DROP TABLE tr_win_stat_tm CASCADE CONSTRAINTS;

DROP TABLE tr_win_stat_src CASCADE CONSTRAINTS;

DROP TABLE tr_win_stat_load CASCADE CONSTRAINTS;

-- 14.1 人工窗口概览表
CREATE TABLE tr_win_stat_ov (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    register_count  NUMBER(10)      DEFAULT 0,          -- 挂号人次
    payment_count   NUMBER(10)      DEFAULT 0,          -- 收费人次
    refund_count    NUMBER(10)      DEFAULT 0,          -- 退费人次
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 14.2 人工窗口年龄分析表
CREATE TABLE tr_win_stat_age (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    age_group       VARCHAR2(50)    NOT NULL,           -- 年龄段
    patient_count   NUMBER(10)      DEFAULT 0,          -- 人数
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 14.3 人工窗口时段分析表
CREATE TABLE tr_win_stat_tm (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    time_slot       VARCHAR2(50)    NOT NULL,           -- 时段
    business_count  NUMBER(10)      DEFAULT 0,          -- 业务量
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 14.4 人工窗口来源分析表
CREATE TABLE tr_win_stat_src (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    source_name     VARCHAR2(100)   NOT NULL,           -- 来源名称
    source_count    NUMBER(10)      DEFAULT 0,          -- 数量
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 14.5 人工窗口工作量表
CREATE TABLE tr_win_stat_load (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    business_type   VARCHAR2(100)   NOT NULL,           -- 业务类型
    register_count  NUMBER(10)      DEFAULT 0,          -- 挂号数
    payment_count   NUMBER(10)      DEFAULT 0,          -- 收费数
    refund_count    NUMBER(10)      DEFAULT 0,          -- 退费数
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 创建索引

CREATE INDEX idx_tr_window_overview_date ON tr_win_stat_ov(stat_date);

-- 添加注释

COMMENT ON TABLE tr_win_stat_ov IS '人工窗口统计-概览';
COMMENT ON TABLE tr_win_stat_ov IS '人工窗口统计-概览(人工窗口业务量的总览指标:挂号/收费/退费人次)';
COMMENT ON COLUMN tr_win_stat_ov.id IS '主键ID';
COMMENT ON COLUMN tr_win_stat_ov.stat_date IS '统计日期';
COMMENT ON COLUMN tr_win_stat_ov.register_count IS '挂号人次';
COMMENT ON COLUMN tr_win_stat_ov.payment_count IS '收费人次';
COMMENT ON COLUMN tr_win_stat_ov.refund_count IS '退费人次';
COMMENT ON COLUMN tr_win_stat_ov.create_time IS '创建时间';
COMMENT ON COLUMN tr_win_stat_ov.update_time IS '更新时间';
COMMENT ON COLUMN tr_win_stat_ov.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_win_stat_ov.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_win_stat_ov.ext3 IS '扩展字段3';
COMMENT ON TABLE tr_win_stat_age IS '人工窗口统计-年龄分析';
COMMENT ON TABLE tr_win_stat_age IS '人工窗口统计-年龄分析(按年龄段分析窗口业务分布)';
COMMENT ON COLUMN tr_win_stat_age.id IS '主键ID';
COMMENT ON COLUMN tr_win_stat_age.stat_date IS '统计日期';
COMMENT ON COLUMN tr_win_stat_age.age_group IS '年龄段';
COMMENT ON COLUMN tr_win_stat_age.patient_count IS '人数';
COMMENT ON COLUMN tr_win_stat_age.create_time IS '创建时间';
COMMENT ON COLUMN tr_win_stat_age.update_time IS '更新时间';
COMMENT ON COLUMN tr_win_stat_age.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_win_stat_age.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_win_stat_age.ext3 IS '扩展字段3';
COMMENT ON TABLE tr_win_stat_tm IS '人工窗口统计-时段分析';
COMMENT ON TABLE tr_win_stat_tm IS '人工窗口统计-时段分析(按时段分析窗口业务量分布)';
COMMENT ON COLUMN tr_win_stat_tm.id IS '主键ID';
COMMENT ON COLUMN tr_win_stat_tm.stat_date IS '统计日期';
COMMENT ON COLUMN tr_win_stat_tm.time_slot IS '时段';
COMMENT ON COLUMN tr_win_stat_tm.business_count IS '业务量';
COMMENT ON COLUMN tr_win_stat_tm.create_time IS '创建时间';
COMMENT ON COLUMN tr_win_stat_tm.update_time IS '更新时间';
COMMENT ON COLUMN tr_win_stat_tm.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_win_stat_tm.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_win_stat_tm.ext3 IS '扩展字段3';
COMMENT ON TABLE tr_win_stat_src IS '人工窗口统计-来源分析';
COMMENT ON TABLE tr_win_stat_src IS '人工窗口统计-来源分析(按患者来源分析窗口业务分布)';
COMMENT ON COLUMN tr_win_stat_src.id IS '主键ID';
COMMENT ON COLUMN tr_win_stat_src.stat_date IS '统计日期';
COMMENT ON COLUMN tr_win_stat_src.source_name IS '来源名称';
COMMENT ON COLUMN tr_win_stat_src.source_count IS '数量';
COMMENT ON COLUMN tr_win_stat_src.create_time IS '创建时间';
COMMENT ON COLUMN tr_win_stat_src.update_time IS '更新时间';
COMMENT ON COLUMN tr_win_stat_src.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_win_stat_src.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_win_stat_src.ext3 IS '扩展字段3';
COMMENT ON TABLE tr_win_stat_load IS '人工窗口统计-工作量';
COMMENT ON TABLE tr_win_stat_load IS '人工窗口统计-工作量(按业务类型统计窗口工作量:挂号/收费/退费)';
COMMENT ON COLUMN tr_win_stat_load.id IS '主键ID';
COMMENT ON COLUMN tr_win_stat_load.stat_date IS '统计日期';
COMMENT ON COLUMN tr_win_stat_load.business_type IS '业务类型';
COMMENT ON COLUMN tr_win_stat_load.register_count IS '挂号数';
COMMENT ON COLUMN tr_win_stat_load.payment_count IS '收费数';
COMMENT ON COLUMN tr_win_stat_load.refund_count IS '退费数';
COMMENT ON COLUMN tr_win_stat_load.create_time IS '创建时间';
COMMENT ON COLUMN tr_win_stat_load.update_time IS '更新时间';
COMMENT ON COLUMN tr_win_stat_load.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_win_stat_load.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_win_stat_load.ext3 IS '扩展字段3';
