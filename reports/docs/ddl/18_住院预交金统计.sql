-- ============================================================
-- 住院预交金统计
-- ============================================================

-- 清理已存在对象(如重建请先执行)

DROP TABLE tr_inpat_prepay_ov CASCADE CONSTRAINTS;

DROP TABLE tr_inpat_prepay_dtl CASCADE CONSTRAINTS;

DROP TABLE tr_inpat_prepay_cht CASCADE CONSTRAINTS;

-- 18.1 住院预交金概览表
CREATE TABLE tr_inpat_prepay_ov (
    id                      NUMBER(19)      PRIMARY KEY,
    stat_date               DATE            NOT NULL,   -- 统计日期
    prepayment_count        NUMBER(10)      DEFAULT 0,  -- 预交金笔数
    prepayment_count_compare NUMBER(10)     DEFAULT 0,  -- 预交金笔数对比
    prepayment_amount       NUMBER(18,2),               -- 预交金金额
    prepayment_amount_compare NUMBER(10)    DEFAULT 0,  -- 预交金金额对比
    create_time             DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time             DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1                    VARCHAR2(500),                        -- 扩展字段1
    ext2                    VARCHAR2(500),                        -- 扩展字段2
    ext3                    VARCHAR2(500)                         -- 扩展字段3
);

-- 18.2 住院预交金日明细表
CREATE TABLE tr_inpat_prepay_dtl (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    item_date       DATE            NOT NULL,           -- 日期
    data_type       VARCHAR2(20)    NOT NULL,           -- 数据类型(SUMMARY/INCOME/REFUND)
    count_last      NUMBER(10)      DEFAULT 0,          -- 上期笔数
    count_current   NUMBER(10)      DEFAULT 0,          -- 本期笔数
    count_compare   NUMBER(10)      DEFAULT 0,          -- 笔数对比
    amount_last     NUMBER(18,2),                       -- 上期金额
    amount_current  NUMBER(18,2),                       -- 本期金额
    amount_compare  NUMBER(10)      DEFAULT 0,          -- 金额对比
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 18.3 住院预交金图表数据表
CREATE TABLE tr_inpat_prepay_cht (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    chart_type      VARCHAR2(50)    NOT NULL,           -- 图表类型(TREND/CHANNEL/PAY_TYPE)
    chart_title     VARCHAR2(200),                      -- 图表标题
    chart_subtitle  VARCHAR2(200),                      -- 副标题
    date_range      VARCHAR2(100),                      -- 日期范围
    category        VARCHAR2(100)   NOT NULL,           -- 分类
    series_name     VARCHAR2(100),                      -- 系列名称
    data_value      NUMBER(10)      DEFAULT 0,          -- 数值
    compare_value   NUMBER(10)      DEFAULT 0,          -- 对比值
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 创建索引

CREATE INDEX idx_tr_prepay_overview_date ON tr_inpat_prepay_ov(stat_date);
CREATE INDEX idx_tr_prepay_detail_date ON tr_inpat_prepay_dtl(stat_date);
CREATE INDEX idx_tr_prepay_chart_date ON tr_inpat_prepay_cht(stat_date);
CREATE INDEX IDX_FIN_CLINIC_VISIT ON TR_OUTP_FIN_CLINIC_MASTER(VISIT_DATE);
CREATE INDEX IDX_FIN_RCPT_VISIT ON TR_OUTP_FIN_RCPT_ACCT(VISIT_DATE);
CREATE INDEX IDX_FIN_RCPT_PID_RCPT ON TR_OUTP_FIN_RCPT_ACCT(PATIENT_ID, RCPT_NO);
CREATE INDEX IDX_FIN_ACCT_DATE ON TR_OUTP_FIN_ACCT_MASTER(ACCT_DATE);
CREATE INDEX IDX_FIN_PAY_VISIT ON TR_OUTP_FIN_PAYMENTS_MONEY(RCPT_NO);
CREATE INDEX IDX_FIN_QUEUE_SCHEDULE ON TR_OUTP_FIN_MOP_QUEUE(SCHEDULE_ID);
CREATE INDEX IDX_FIN_QUEUE_VISIT ON TR_OUTP_FIN_MOP_QUEUE(VISIT_DATE);

-- 添加注释

COMMENT ON TABLE tr_inpat_prepay_ov IS '住院预交金统计-概览(存储住院预交金总览:预交金笔数、金额及对比值)';
COMMENT ON COLUMN tr_inpat_prepay_ov.id IS '主键ID';
COMMENT ON COLUMN tr_inpat_prepay_ov.stat_date IS '统计日期';
COMMENT ON COLUMN tr_inpat_prepay_ov.prepayment_count IS '预交金笔数';
COMMENT ON COLUMN tr_inpat_prepay_ov.prepayment_count_compare IS '预交金笔数对比值';
COMMENT ON COLUMN tr_inpat_prepay_ov.prepayment_amount IS '预交金金额';
COMMENT ON COLUMN tr_inpat_prepay_ov.prepayment_amount_compare IS '预交金金额对比值';
COMMENT ON COLUMN tr_inpat_prepay_ov.create_time IS '创建时间';
COMMENT ON COLUMN tr_inpat_prepay_ov.update_time IS '更新时间';
COMMENT ON COLUMN tr_inpat_prepay_ov.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_inpat_prepay_ov.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_inpat_prepay_ov.ext3 IS '扩展字段3';
COMMENT ON TABLE tr_inpat_prepay_dtl IS '住院预交金统计-日明细(按日期和数据类型存储预交金笔数和金额的明细对比)';
COMMENT ON COLUMN tr_inpat_prepay_dtl.id IS '主键ID';
COMMENT ON COLUMN tr_inpat_prepay_dtl.stat_date IS '统计日期';
COMMENT ON COLUMN tr_inpat_prepay_dtl.item_date IS '日期';
COMMENT ON COLUMN tr_inpat_prepay_dtl.data_type IS '数据类型(SUMMARY/INCOME/REFUND)';
COMMENT ON COLUMN tr_inpat_prepay_dtl.count_last IS '上期笔数';
COMMENT ON COLUMN tr_inpat_prepay_dtl.count_current IS '本期笔数';
COMMENT ON COLUMN tr_inpat_prepay_dtl.count_compare IS '笔数对比';
COMMENT ON COLUMN tr_inpat_prepay_dtl.amount_last IS '上期金额';
COMMENT ON COLUMN tr_inpat_prepay_dtl.amount_current IS '本期金额';
COMMENT ON COLUMN tr_inpat_prepay_dtl.amount_compare IS '金额对比';
COMMENT ON COLUMN tr_inpat_prepay_dtl.create_time IS '创建时间';
COMMENT ON COLUMN tr_inpat_prepay_dtl.update_time IS '更新时间';
COMMENT ON COLUMN tr_inpat_prepay_dtl.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_inpat_prepay_dtl.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_inpat_prepay_dtl.ext3 IS '扩展字段3';
COMMENT ON TABLE tr_inpat_prepay_cht IS '住院预交金统计-图表(存储预交金趋势、渠道、支付方式等图表数据)';
COMMENT ON COLUMN tr_inpat_prepay_cht.id IS '主键ID';
COMMENT ON COLUMN tr_inpat_prepay_cht.stat_date IS '统计日期';
COMMENT ON COLUMN tr_inpat_prepay_cht.chart_type IS '图表类型(TREND/CHANNEL/PAY_TYPE)';
COMMENT ON COLUMN tr_inpat_prepay_cht.chart_title IS '图表标题';
COMMENT ON COLUMN tr_inpat_prepay_cht.chart_subtitle IS '副标题';
COMMENT ON COLUMN tr_inpat_prepay_cht.date_range IS '日期范围';
COMMENT ON COLUMN tr_inpat_prepay_cht.category IS '分类';
COMMENT ON COLUMN tr_inpat_prepay_cht.series_name IS '系列名称';
COMMENT ON COLUMN tr_inpat_prepay_cht.data_value IS '数值';
COMMENT ON COLUMN tr_inpat_prepay_cht.compare_value IS '对比值';
COMMENT ON COLUMN tr_inpat_prepay_cht.create_time IS '创建时间';
COMMENT ON COLUMN tr_inpat_prepay_cht.update_time IS '更新时间';
COMMENT ON COLUMN tr_inpat_prepay_cht.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_inpat_prepay_cht.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_inpat_prepay_cht.ext3 IS '扩展字段3';
COMMENT ON TABLE TR_OUTP_FIN_CLINIC_MASTER IS '门诊财务报表-就诊主表日快照';
COMMENT ON TABLE TR_OUTP_FIN_RCPT_ACCT IS '门诊财务报表-收据明细（人次去重、bt1/3/4/6/8）';
COMMENT ON TABLE TR_OUTP_FIN_ACCT_MASTER IS '门诊财务报表-结账汇总（收据张数/金额唯一口径）';
COMMENT ON TABLE TR_OUTP_FIN_PAYMENTS_MONEY IS '门诊财务报表-支付方式明细（bt5/7/9）';
COMMENT ON TABLE TR_OUTP_FIN_MOP_QUEUE IS '门诊财务报表-排班队列（bt2取号渠道分析）';
