-- ============================================================
-- 检验统计
-- ============================================================

-- 清理已存在对象(如重建请先执行)

DROP TABLE tr_labstat_ov CASCADE CONSTRAINTS;

DROP TABLE tr_labstat_rnk CASCADE CONSTRAINTS;

DROP TABLE tr_labstat_tm CASCADE CONSTRAINTS;

-- 5.1 检验统计概览表
CREATE TABLE tr_labstat_ov (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    blood_collection NUMBER(10)     DEFAULT 0,          -- 采血人次
    blood_efficiency VARCHAR2(20),                     -- 采血效率
    lab_efficiency  VARCHAR2(20),                      -- 检验效率
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 5.2 检验统计排行表
CREATE TABLE tr_labstat_rnk (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    rank_type       VARCHAR2(50)    NOT NULL,           -- 排行类型(BLOOD/LAB)
    rank_num        NUMBER(5)       DEFAULT 0,          -- 排名
    item_name       VARCHAR2(100)   NOT NULL,           -- 项目名称
    item_value      NUMBER(10)      DEFAULT 0,          -- 项目值
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 5.3 检验统计时段分析表
CREATE TABLE tr_labstat_tm (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    time_slot       VARCHAR2(50)    NOT NULL,           -- 时段
    blood_count     NUMBER(10)      DEFAULT 0,          -- 采血人次
    lab_count       NUMBER(10)      DEFAULT 0,          -- 检验人次
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 创建索引

CREATE INDEX idx_tr_lab_overview_date ON tr_labstat_ov(stat_date);

-- 添加注释

COMMENT ON TABLE tr_labstat_ov IS '检验统计-概览';
COMMENT ON TABLE tr_labstat_ov IS '检验统计-概览(检验科运营总览指标:采血人次、采血效率、检验效率)';
COMMENT ON COLUMN tr_labstat_ov.id IS '主键ID';
COMMENT ON COLUMN tr_labstat_ov.stat_date IS '统计日期';
COMMENT ON COLUMN tr_labstat_ov.blood_collection IS '采血人次';
COMMENT ON COLUMN tr_labstat_ov.blood_efficiency IS '采血效率';
COMMENT ON COLUMN tr_labstat_ov.lab_efficiency IS '检验效率';
COMMENT ON COLUMN tr_labstat_ov.create_time IS '创建时间';
COMMENT ON COLUMN tr_labstat_ov.update_time IS '更新时间';
COMMENT ON COLUMN tr_labstat_ov.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_labstat_ov.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_labstat_ov.ext3 IS '扩展字段3';
COMMENT ON TABLE tr_labstat_rnk IS '检验统计-排行';
COMMENT ON TABLE tr_labstat_rnk IS '检验统计-排行(检验项目或科室的排行数据:采血排行/检验排行)';
COMMENT ON COLUMN tr_labstat_rnk.id IS '主键ID';
COMMENT ON COLUMN tr_labstat_rnk.stat_date IS '统计日期';
COMMENT ON COLUMN tr_labstat_rnk.rank_type IS '排行类型(BLOOD/LAB)';
COMMENT ON COLUMN tr_labstat_rnk.rank_num IS '排名';
COMMENT ON COLUMN tr_labstat_rnk.item_name IS '项目名称';
COMMENT ON COLUMN tr_labstat_rnk.item_value IS '项目值';
COMMENT ON COLUMN tr_labstat_rnk.create_time IS '创建时间';
COMMENT ON COLUMN tr_labstat_rnk.update_time IS '更新时间';
COMMENT ON COLUMN tr_labstat_rnk.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_labstat_rnk.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_labstat_rnk.ext3 IS '扩展字段3';
COMMENT ON TABLE tr_labstat_tm IS '检验统计-时段分析';
COMMENT ON TABLE tr_labstat_tm IS '检验统计-时段分析(检验科各时段的业务量分布)';
COMMENT ON COLUMN tr_labstat_tm.id IS '主键ID';
COMMENT ON COLUMN tr_labstat_tm.stat_date IS '统计日期';
COMMENT ON COLUMN tr_labstat_tm.time_slot IS '时段';
COMMENT ON COLUMN tr_labstat_tm.blood_count IS '采血人次';
COMMENT ON COLUMN tr_labstat_tm.lab_count IS '检验人次';
COMMENT ON COLUMN tr_labstat_tm.create_time IS '创建时间';
COMMENT ON COLUMN tr_labstat_tm.update_time IS '更新时间';
COMMENT ON COLUMN tr_labstat_tm.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_labstat_tm.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_labstat_tm.ext3 IS '扩展字段3';
