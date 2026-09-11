-- ============================================================
-- 出院结算报表
-- ============================================================

-- 清理已存在对象(如重建请先执行)

DROP TABLE tr_disch_settle_ov CASCADE CONSTRAINTS;

DROP TABLE tr_disch_settle_dtl CASCADE CONSTRAINTS;

DROP TABLE tr_disch_settle_cht CASCADE CONSTRAINTS;

-- 16.1 出院结算概览表
CREATE TABLE tr_disch_settle_ov (
    id                      NUMBER(19)      PRIMARY KEY,
    stat_date               DATE            NOT NULL,   -- 统计日期
    total_discharge_count   NUMBER(10)      DEFAULT 0,  -- 总出院人次
    total_discharge_compare NUMBER(10)      DEFAULT 0,  -- 总出院对比
    discharged_count        NUMBER(10)      DEFAULT 0,  -- 已出院人次
    discharged_compare      NUMBER(10)      DEFAULT 0,  -- 已出院对比
    not_discharged_count    NUMBER(10)      DEFAULT 0,  -- 未出院人次
    not_discharged_compare  NUMBER(10)      DEFAULT 0,  -- 未出院对比
    settlement_amount       NUMBER(18,2),               -- 结算金额
    settlement_amount_compare NUMBER(10)    DEFAULT 0,  -- 结算金额对比
    create_time             DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time             DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1                    VARCHAR2(500),                        -- 扩展字段1
    ext2                    VARCHAR2(500),                        -- 扩展字段2
    ext3                    VARCHAR2(500)                         -- 扩展字段3
);

-- 16.2 出院结算日明细表
CREATE TABLE tr_disch_settle_dtl (
    id                      NUMBER(19)      PRIMARY KEY,
    stat_date               DATE            NOT NULL,   -- 统计日期
    item_date               DATE            NOT NULL,   -- 日期
    total_last              NUMBER(10)      DEFAULT 0,  -- 总出院上期
    total_current           NUMBER(10)      DEFAULT 0,  -- 总出院本期
    total_compare           NUMBER(10)      DEFAULT 0,  -- 总出院对比
    discharged_last         NUMBER(10)      DEFAULT 0,  -- 已出院上期
    discharged_current      NUMBER(10)      DEFAULT 0,  -- 已出院本期
    discharged_compare      NUMBER(10)      DEFAULT 0,  -- 已出院对比
    not_discharged_last     NUMBER(10)      DEFAULT 0,  -- 未出院上期
    not_discharged_current  NUMBER(10)      DEFAULT 0,  -- 未出院本期
    not_discharged_compare  NUMBER(10)      DEFAULT 0,  -- 未出院对比
    amount_last             NUMBER(18,2),               -- 金额上期
    amount_current          NUMBER(18,2),               -- 金额本期
    amount_compare          NUMBER(10)      DEFAULT 0,  -- 金额对比
    create_time             DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time             DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1                    VARCHAR2(500),                        -- 扩展字段1
    ext2                    VARCHAR2(500),                        -- 扩展字段2
    ext3                    VARCHAR2(500)                         -- 扩展字段3
);

-- 16.3 出院结算图表分析表
CREATE TABLE tr_disch_settle_cht (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    chart_type      VARCHAR2(50)    NOT NULL,           -- 图表类型(CHANNEL/PATIENT_TYPE/AMOUNT_TYPE)
    item_name       VARCHAR2(100)   NOT NULL,           -- 项目名称
    item_value      NUMBER(10)      DEFAULT 0,          -- 数值
    item_compare    NUMBER(10)      DEFAULT 0,          -- 对比值
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 创建索引

CREATE INDEX idx_tr_discharge_overview_date ON tr_disch_settle_ov(stat_date);

-- 添加注释

COMMENT ON TABLE tr_disch_settle_ov IS '出院结算报表-概览';
COMMENT ON TABLE tr_disch_settle_ov IS '出院结算报表-概览(出院结算业务的总览指标:出院人次、结算金额)';
COMMENT ON COLUMN tr_disch_settle_ov.id IS '主键ID';
COMMENT ON COLUMN tr_disch_settle_ov.stat_date IS '统计日期';
COMMENT ON COLUMN tr_disch_settle_ov.total_discharge_count IS '总出院人次';
COMMENT ON COLUMN tr_disch_settle_ov.total_discharge_compare IS '总出院对比';
COMMENT ON COLUMN tr_disch_settle_ov.discharged_count IS '已出院人次';
COMMENT ON COLUMN tr_disch_settle_ov.discharged_compare IS '已出院对比';
COMMENT ON COLUMN tr_disch_settle_ov.not_discharged_count IS '未出院人次';
COMMENT ON COLUMN tr_disch_settle_ov.not_discharged_compare IS '未出院对比';
COMMENT ON COLUMN tr_disch_settle_ov.settlement_amount IS '结算金额';
COMMENT ON COLUMN tr_disch_settle_ov.settlement_amount_compare IS '结算金额对比';
COMMENT ON COLUMN tr_disch_settle_ov.create_time IS '创建时间';
COMMENT ON COLUMN tr_disch_settle_ov.update_time IS '更新时间';
COMMENT ON COLUMN tr_disch_settle_ov.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_disch_settle_ov.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_disch_settle_ov.ext3 IS '扩展字段3';
COMMENT ON TABLE tr_disch_settle_dtl IS '出院结算报表-日明细';
COMMENT ON TABLE tr_disch_settle_dtl IS '出院结算报表-日明细(出院结算按日期的详细对比数据:本期 vs 上期)';
COMMENT ON COLUMN tr_disch_settle_dtl.id IS '主键ID';
COMMENT ON COLUMN tr_disch_settle_dtl.stat_date IS '统计日期';
COMMENT ON COLUMN tr_disch_settle_dtl.item_date IS '日期';
COMMENT ON COLUMN tr_disch_settle_dtl.total_last IS '总出院上期';
COMMENT ON COLUMN tr_disch_settle_dtl.total_current IS '总出院本期';
COMMENT ON COLUMN tr_disch_settle_dtl.total_compare IS '总出院对比';
COMMENT ON COLUMN tr_disch_settle_dtl.discharged_last IS '已出院上期';
COMMENT ON COLUMN tr_disch_settle_dtl.discharged_current IS '已出院本期';
COMMENT ON COLUMN tr_disch_settle_dtl.discharged_compare IS '已出院对比';
COMMENT ON COLUMN tr_disch_settle_dtl.not_discharged_last IS '未出院上期';
COMMENT ON COLUMN tr_disch_settle_dtl.not_discharged_current IS '未出院本期';
COMMENT ON COLUMN tr_disch_settle_dtl.not_discharged_compare IS '未出院对比';
COMMENT ON COLUMN tr_disch_settle_dtl.amount_last IS '金额上期';
COMMENT ON COLUMN tr_disch_settle_dtl.amount_current IS '金额本期';
COMMENT ON COLUMN tr_disch_settle_dtl.amount_compare IS '金额对比';
COMMENT ON COLUMN tr_disch_settle_dtl.create_time IS '创建时间';
COMMENT ON COLUMN tr_disch_settle_dtl.update_time IS '更新时间';
COMMENT ON COLUMN tr_disch_settle_dtl.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_disch_settle_dtl.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_disch_settle_dtl.ext3 IS '扩展字段3';
COMMENT ON TABLE tr_disch_settle_cht IS '出院结算报表-图表';
COMMENT ON TABLE tr_disch_settle_cht IS '出院结算报表-图表(出院结算图表数据:按图表类型和项目分类存储)';
COMMENT ON COLUMN tr_disch_settle_cht.id IS '主键ID';
COMMENT ON COLUMN tr_disch_settle_cht.stat_date IS '统计日期';
COMMENT ON COLUMN tr_disch_settle_cht.chart_type IS '图表类型(CHANNEL/PATIENT_TYPE/AMOUNT_TYPE)';
COMMENT ON COLUMN tr_disch_settle_cht.item_name IS '项目名称';
COMMENT ON COLUMN tr_disch_settle_cht.item_value IS '数值';
COMMENT ON COLUMN tr_disch_settle_cht.item_compare IS '对比值';
COMMENT ON COLUMN tr_disch_settle_cht.create_time IS '创建时间';
COMMENT ON COLUMN tr_disch_settle_cht.update_time IS '更新时间';
COMMENT ON COLUMN tr_disch_settle_cht.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_disch_settle_cht.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_disch_settle_cht.ext3 IS '扩展字段3';
