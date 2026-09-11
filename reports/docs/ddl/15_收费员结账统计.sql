-- ============================================================
-- 收费员结账统计
-- ============================================================

-- 清理已存在对象(如重建请先执行)

DROP TABLE tr_cash_settle_ov CASCADE CONSTRAINTS;

DROP TABLE tr_cash_settle_dtl CASCADE CONSTRAINTS;

DROP TABLE tr_cash_settle_cht CASCADE CONSTRAINTS;

-- 15.1 收费员结账概览表
CREATE TABLE tr_cash_settle_ov (
    id                      NUMBER(19)      PRIMARY KEY,
    stat_date               DATE            NOT NULL,   -- 统计日期
    appointment_register    NUMBER(10)      DEFAULT 0,  -- 预约挂号
    appointment_register_compare NUMBER(10) DEFAULT 0, -- 对比值
    appointment_fetch     NUMBER(10)      DEFAULT 0,  -- 预约取号
    appointment_fetch_compare NUMBER(10) DEFAULT 0,  -- 预约取号对比
    today_register        NUMBER(10)      DEFAULT 0,  -- 当日挂号
    today_register_compare NUMBER(10) DEFAULT 0,  -- 当日挂号对比
    refund                NUMBER(10)      DEFAULT 0,  -- 退号
    refund_compare        NUMBER(10)      DEFAULT 0,  -- 退号对比
    outpatient_charge     NUMBER(10)      DEFAULT 0,  -- 门诊收费
    outpatient_charge_compare NUMBER(10) DEFAULT 0,  -- 门诊收费对比
    outpatient_refund     NUMBER(10)      DEFAULT 0,  -- 门诊退费
    outpatient_refund_compare NUMBER(10) DEFAULT 0,  -- 门诊退费对比
    prepayment            NUMBER(10)      DEFAULT 0,  -- 预交金
    prepayment_compare    NUMBER(10)      DEFAULT 0,  -- 预交金对比
    hospital_refund       NUMBER(10)      DEFAULT 0,  -- 住院退费
    hospital_refund_compare NUMBER(10) DEFAULT 0,  -- 住院退费对比
    discharge_settlement  NUMBER(10)      DEFAULT 0,  -- 出院结算
    discharge_settlement_compare NUMBER(10) DEFAULT 0,  -- 出院结算对比
    create_time           DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time           DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1                  VARCHAR2(500),                        -- 扩展字段1
    ext2                  VARCHAR2(500),                        -- 扩展字段2
    ext3                  VARCHAR2(500)                         -- 扩展字段3
);

-- 15.2 收费员结账日明细表
CREATE TABLE tr_cash_settle_dtl (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    item_date       DATE            NOT NULL,           -- 日期
    cashier_name    VARCHAR2(100),                      -- 收费员
    item_type       VARCHAR2(50)    NOT NULL,           -- 项目类型
    item_value      NUMBER(18,2),                       -- 金额
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 15.3 收费员结账图表数据表
CREATE TABLE tr_cash_settle_cht (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    chart_title     VARCHAR2(200),                      -- 图表标题
    chart_subtitle  VARCHAR2(200),                      -- 副标题
    date_range      VARCHAR2(100),                      -- 日期范围
    category        VARCHAR2(100)   NOT NULL,           -- 分类
    data_value      NUMBER(10)      DEFAULT 0,          -- 数值
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 创建索引

CREATE INDEX idx_tr_cashier_overview_date ON tr_cash_settle_ov(stat_date);

-- 添加注释

COMMENT ON TABLE tr_cash_settle_ov IS '收费员结账统计-概览';
COMMENT ON TABLE tr_cash_settle_ov IS '收费员结账统计-概览(收费员结账业务的总览指标:各类业务笔数及对比值)';
COMMENT ON COLUMN tr_cash_settle_ov.id IS '主键ID';
COMMENT ON COLUMN tr_cash_settle_ov.stat_date IS '统计日期';
COMMENT ON COLUMN tr_cash_settle_ov.appointment_register IS '预约挂号';
COMMENT ON COLUMN tr_cash_settle_ov.appointment_register_compare IS '预约挂号对比';
COMMENT ON COLUMN tr_cash_settle_ov.appointment_fetch IS '预约取号';
COMMENT ON COLUMN tr_cash_settle_ov.appointment_fetch_compare IS '预约取号对比';
COMMENT ON COLUMN tr_cash_settle_ov.today_register IS '当日挂号';
COMMENT ON COLUMN tr_cash_settle_ov.today_register_compare IS '当日挂号对比';
COMMENT ON COLUMN tr_cash_settle_ov.refund IS '退号';
COMMENT ON COLUMN tr_cash_settle_ov.refund_compare IS '退号对比';
COMMENT ON COLUMN tr_cash_settle_ov.outpatient_charge IS '门诊收费';
COMMENT ON COLUMN tr_cash_settle_ov.outpatient_charge_compare IS '门诊收费对比';
COMMENT ON COLUMN tr_cash_settle_ov.outpatient_refund IS '门诊退费';
COMMENT ON COLUMN tr_cash_settle_ov.outpatient_refund_compare IS '门诊退费对比';
COMMENT ON COLUMN tr_cash_settle_ov.prepayment IS '预交金';
COMMENT ON COLUMN tr_cash_settle_ov.prepayment_compare IS '预交金对比';
COMMENT ON COLUMN tr_cash_settle_ov.hospital_refund IS '住院退费';
COMMENT ON COLUMN tr_cash_settle_ov.hospital_refund_compare IS '住院退费对比';
COMMENT ON COLUMN tr_cash_settle_ov.discharge_settlement IS '出院结算';
COMMENT ON COLUMN tr_cash_settle_ov.discharge_settlement_compare IS '出院结算对比';
COMMENT ON COLUMN tr_cash_settle_ov.create_time IS '创建时间';
COMMENT ON COLUMN tr_cash_settle_ov.update_time IS '更新时间';
COMMENT ON COLUMN tr_cash_settle_ov.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_cash_settle_ov.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_cash_settle_ov.ext3 IS '扩展字段3';
COMMENT ON TABLE tr_cash_settle_dtl IS '收费员结账统计-日明细';
COMMENT ON TABLE tr_cash_settle_dtl IS '收费员结账统计-日明细(收费员每日结账的明细记录)';
COMMENT ON COLUMN tr_cash_settle_dtl.id IS '主键ID';
COMMENT ON COLUMN tr_cash_settle_dtl.stat_date IS '统计日期';
COMMENT ON COLUMN tr_cash_settle_dtl.item_date IS '日期';
COMMENT ON COLUMN tr_cash_settle_dtl.cashier_name IS '收费员';
COMMENT ON COLUMN tr_cash_settle_dtl.item_type IS '项目类型';
COMMENT ON COLUMN tr_cash_settle_dtl.item_value IS '金额';
COMMENT ON COLUMN tr_cash_settle_dtl.create_time IS '创建时间';
COMMENT ON COLUMN tr_cash_settle_dtl.update_time IS '更新时间';
COMMENT ON COLUMN tr_cash_settle_dtl.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_cash_settle_dtl.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_cash_settle_dtl.ext3 IS '扩展字段3';
COMMENT ON TABLE tr_cash_settle_cht IS '收费员结账统计-图表';
COMMENT ON TABLE tr_cash_settle_cht IS '收费员结账统计-图表(收费员结账图表所需的结构化数据)';
COMMENT ON COLUMN tr_cash_settle_cht.id IS '主键ID';
COMMENT ON COLUMN tr_cash_settle_cht.stat_date IS '统计日期';
COMMENT ON COLUMN tr_cash_settle_cht.chart_title IS '图表标题';
COMMENT ON COLUMN tr_cash_settle_cht.chart_subtitle IS '副标题';
COMMENT ON COLUMN tr_cash_settle_cht.date_range IS '日期范围';
COMMENT ON COLUMN tr_cash_settle_cht.category IS '分类';
COMMENT ON COLUMN tr_cash_settle_cht.data_value IS '数值';
COMMENT ON COLUMN tr_cash_settle_cht.create_time IS '创建时间';
COMMENT ON COLUMN tr_cash_settle_cht.update_time IS '更新时间';
COMMENT ON COLUMN tr_cash_settle_cht.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_cash_settle_cht.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_cash_settle_cht.ext3 IS '扩展字段3';
