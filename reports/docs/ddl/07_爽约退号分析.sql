-- ============================================================
-- 爽约退号分析
-- ============================================================

-- 清理已存在对象(如重建请先执行)

DROP TABLE tr_noshow_ov CASCADE CONSTRAINTS;

DROP TABLE tr_noshow_dtl CASCADE CONSTRAINTS;

DROP TABLE tr_noshow_org CASCADE CONSTRAINTS;

DROP TABLE tr_noshow_chn CASCADE CONSTRAINTS;

DROP TABLE tr_noshow_age CASCADE CONSTRAINTS;

-- 7.1 爽约退号概览表
CREATE TABLE tr_noshow_ov (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    refund_count    NUMBER(10)      DEFAULT 0,          -- 退号人数
    refund_rate     VARCHAR2(20),                      -- 退号率
    no_show_count   NUMBER(10)      DEFAULT 0,          -- 爽约人数
    no_show_rate    VARCHAR2(20),                      -- 爽约率
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 7.2 爽约退号科室明细表
CREATE TABLE tr_noshow_dtl (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    dept_name       VARCHAR2(100)   NOT NULL,           -- 科室名称
    refund_count    NUMBER(10)      DEFAULT 0,          -- 退号人数
    refund_rate     VARCHAR2(20),                      -- 退号率
    no_show_count   NUMBER(10)      DEFAULT 0,          -- 爽约人数
    no_show_rate    VARCHAR2(20),                      -- 爽约率
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 7.3 爽约退号来源分析表
CREATE TABLE tr_noshow_org (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    origin_type     VARCHAR2(50)    NOT NULL,           -- 来源类型(REFUND/NO_SHOW)
    item_name       VARCHAR2(100)   NOT NULL,           -- 来源名称
    item_value      NUMBER(10)      DEFAULT 0,          -- 数量
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 7.4 爽约退号渠道分析表
CREATE TABLE tr_noshow_chn (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    channel_type    VARCHAR2(50)    NOT NULL,           -- 渠道类型(REFUND/NO_SHOW)
    item_name       VARCHAR2(100)   NOT NULL,           -- 渠道名称
    item_value      NUMBER(10)      DEFAULT 0,          -- 数量
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 7.5 爽约退号年龄分析表
CREATE TABLE tr_noshow_age (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    age_group       VARCHAR2(50)    NOT NULL,           -- 年龄段
    no_show_count   NUMBER(10)      DEFAULT 0,          -- 爽约人数
    refund_count    NUMBER(10)      DEFAULT 0,          -- 退号人数
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 创建索引

CREATE INDEX idx_tr_noshow_ov_date ON tr_noshow_ov(stat_date);
CREATE INDEX idx_tr_noshow_dtl_dept ON tr_noshow_dtl(dept_name);

-- 添加注释

COMMENT ON TABLE tr_noshow_ov IS '爽约退号分析-概览';
COMMENT ON TABLE tr_noshow_ov IS '爽约退号分析-概览(爽约和退号情况的总览指标)';
COMMENT ON COLUMN tr_noshow_ov.id IS '主键ID';
COMMENT ON COLUMN tr_noshow_ov.stat_date IS '统计日期';
COMMENT ON COLUMN tr_noshow_ov.refund_count IS '退号人数';
COMMENT ON COLUMN tr_noshow_ov.refund_rate IS '退号率';
COMMENT ON COLUMN tr_noshow_ov.no_show_count IS '爽约人数';
COMMENT ON COLUMN tr_noshow_ov.no_show_rate IS '爽约率';
COMMENT ON COLUMN tr_noshow_ov.create_time IS '创建时间';
COMMENT ON COLUMN tr_noshow_ov.update_time IS '更新时间';
COMMENT ON COLUMN tr_noshow_ov.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_noshow_ov.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_noshow_ov.ext3 IS '扩展字段3';
COMMENT ON TABLE tr_noshow_dtl IS '爽约退号分析-科室明细';
COMMENT ON TABLE tr_noshow_dtl IS '爽约退号分析-科室明细(按科室维度统计爽约退号数据)';
COMMENT ON COLUMN tr_noshow_dtl.id IS '主键ID';
COMMENT ON COLUMN tr_noshow_dtl.stat_date IS '统计日期';
COMMENT ON COLUMN tr_noshow_dtl.dept_name IS '科室名称';
COMMENT ON COLUMN tr_noshow_dtl.refund_count IS '退号人数';
COMMENT ON COLUMN tr_noshow_dtl.refund_rate IS '退号率';
COMMENT ON COLUMN tr_noshow_dtl.no_show_count IS '爽约人数';
COMMENT ON COLUMN tr_noshow_dtl.no_show_rate IS '爽约率';
COMMENT ON COLUMN tr_noshow_dtl.create_time IS '创建时间';
COMMENT ON COLUMN tr_noshow_dtl.update_time IS '更新时间';
COMMENT ON COLUMN tr_noshow_dtl.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_noshow_dtl.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_noshow_dtl.ext3 IS '扩展字段3';
COMMENT ON TABLE tr_noshow_org IS '爽约退号分析-来源分析';
COMMENT ON TABLE tr_noshow_org IS '爽约退号分析-来源分析(按挂号来源维度分析爽约/退号分布)';
COMMENT ON COLUMN tr_noshow_org.id IS '主键ID';
COMMENT ON COLUMN tr_noshow_org.stat_date IS '统计日期';
COMMENT ON COLUMN tr_noshow_org.origin_type IS '来源类型(REFUND/NO_SHOW)';
COMMENT ON COLUMN tr_noshow_org.item_name IS '来源名称';
COMMENT ON COLUMN tr_noshow_org.item_value IS '数量';
COMMENT ON COLUMN tr_noshow_org.create_time IS '创建时间';
COMMENT ON COLUMN tr_noshow_org.update_time IS '更新时间';
COMMENT ON COLUMN tr_noshow_org.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_noshow_org.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_noshow_org.ext3 IS '扩展字段3';
COMMENT ON TABLE tr_noshow_chn IS '爽约退号分析-渠道分析';
COMMENT ON TABLE tr_noshow_chn IS '爽约退号分析-渠道分析(按渠道维度分析爽约/退号分布)';
COMMENT ON COLUMN tr_noshow_chn.id IS '主键ID';
COMMENT ON COLUMN tr_noshow_chn.stat_date IS '统计日期';
COMMENT ON COLUMN tr_noshow_chn.channel_type IS '渠道类型(REFUND/NO_SHOW)';
COMMENT ON COLUMN tr_noshow_chn.item_name IS '渠道名称';
COMMENT ON COLUMN tr_noshow_chn.item_value IS '数量';
COMMENT ON COLUMN tr_noshow_chn.create_time IS '创建时间';
COMMENT ON COLUMN tr_noshow_chn.update_time IS '更新时间';
COMMENT ON COLUMN tr_noshow_chn.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_noshow_chn.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_noshow_chn.ext3 IS '扩展字段3';
COMMENT ON TABLE tr_noshow_age IS '爽约退号分析-年龄分析';
COMMENT ON TABLE tr_noshow_age IS '爽约退号分析-年龄分析(按年龄段分析爽约/退号分布)';
COMMENT ON COLUMN tr_noshow_age.id IS '主键ID';
COMMENT ON COLUMN tr_noshow_age.stat_date IS '统计日期';
COMMENT ON COLUMN tr_noshow_age.age_group IS '年龄段';
COMMENT ON COLUMN tr_noshow_age.no_show_count IS '爽约人数';
COMMENT ON COLUMN tr_noshow_age.refund_count IS '退号人数';
COMMENT ON COLUMN tr_noshow_age.create_time IS '创建时间';
COMMENT ON COLUMN tr_noshow_age.update_time IS '更新时间';
COMMENT ON COLUMN tr_noshow_age.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_noshow_age.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_noshow_age.ext3 IS '扩展字段3';
