-- ============================================================
-- 出院结算报表
-- 数据来源(HIS抽数存储过程):
--   SP_DischSettle_1028       -> tr_disch_settle_visit (pat_visit,按出院日期)
--   SP_SettleMaster_1029      -> tr_settle_master      (inp_settle_master,按结算日期)
--   SP_SettlePaymentsMoney_1030 -> tr_settle_payments  (master+payments_money,按结算日期)
-- 报表按 stat_date 实时聚合,不再用预聚合宽表
-- ============================================================

-- 清理已存在对象(如重建请先执行)
DROP TABLE tr_disch_settle_visit CASCADE CONSTRAINTS;
DROP TABLE tr_settle_master CASCADE CONSTRAINTS;
DROP TABLE tr_settle_payments CASCADE CONSTRAINTS;
-- 旧预聚合宽表一并清理
DROP TABLE tr_disch_settle_ov CASCADE CONSTRAINTS;
DROP TABLE tr_disch_settle_dtl CASCADE CONSTRAINTS;
DROP TABLE tr_disch_settle_cht CASCADE CONSTRAINTS;

-- 16.1 出院记录表(SP_DischSettle_1028: pat_visit 出院且 DISCHARGE_DATE_TIME 非空)
CREATE TABLE tr_disch_settle_visit (
    stat_date           DATE NOT NULL,                  -- 统计日期(=trunc(DISCHARGE_DATE_TIME) 出院日期)
    settle_status       NUMBER(2),                      -- 状态:1已出院结算 2出院未结算 3结算有余额 4结算欠费 -1删除中间态
    patient_id          VARCHAR2(50) NOT NULL,          -- 患者ID
    visit_id            VARCHAR2(50) NOT NULL,          -- 住院就诊ID
    discharge_date_time DATE,                           -- 出院时间
    total_costs         NUMBER(18,2),                   -- 总费用
    total_payments      NUMBER(18,2),                   -- 总预交金
    create_time         DATE DEFAULT SYSDATE,           -- 创建时间
    update_time         DATE DEFAULT SYSDATE,           -- 更新时间
    ext1                VARCHAR2(500),                  -- 扩展字段1
    ext2                VARCHAR2(500),                  -- 扩展字段2
    ext3                VARCHAR2(500)                   -- 扩展字段3
);

-- 16.2 结算主表(SP_SettleMaster_1029: inp_settle_master 按 SETTLING_DATE)
CREATE TABLE tr_settle_master (
    stat_date     DATE NOT NULL,                        -- 统计日期(=trunc(SETTLING_DATE) 结算日期)
    rcpt_no       VARCHAR2(50) NOT NULL,                -- 结算收据号(每笔结算唯一)
    patient_id    VARCHAR2(50) NOT NULL,                -- 患者ID
    visit_id      VARCHAR2(50) NOT NULL,                -- 住院就诊ID
    settling_date DATE,                                 -- 结算时间
    operator_no   VARCHAR2(50),                         -- 操作员工号(渠道:9111=自助机,其余=窗口)
    charge_type   VARCHAR2(50),                         -- 结算费别(职工医保/异地职工医保/居民医保/自费等)
    costs         NUMBER(18,2),                         -- 费用
    charges       NUMBER(18,2),                         -- 结算金额
    payments      NUMBER(18,2),                         -- 预缴金额
    create_time   DATE DEFAULT SYSDATE,                 -- 创建时间
    update_time   DATE DEFAULT SYSDATE,                 -- 更新时间
    ext1          VARCHAR2(500),                        -- 扩展字段1
    ext2          VARCHAR2(500),                        -- 扩展字段2
    ext3          VARCHAR2(500)                         -- 扩展字段3
);

-- 16.3 结算支付方式表(SP_SettlePaymentsMoney_1030: inp_settle_master LEFT JOIN inp_payments_money)
CREATE TABLE tr_settle_payments (
    stat_date       DATE NOT NULL,                      -- 统计日期(=trunc(SETTLING_DATE) 结算日期)
    rcpt_no         VARCHAR2(50) NOT NULL,              -- 结算收据号(关联结算主表)
    patient_id      VARCHAR2(50),                       -- 患者ID
    visit_id        VARCHAR2(50),                       -- 住院就诊ID
    settling_date   DATE,                               -- 结算时间
    money_type      VARCHAR2(50),                       -- 支付方式(微信/支付宝/银行卡/现金等)
    payment_amount  NUMBER(18,2),                       -- 支付金额(收)
    refunded_amount NUMBER(18,2),                       -- 退费金额(退)
    create_time     DATE DEFAULT SYSDATE,               -- 创建时间
    update_time     DATE DEFAULT SYSDATE,               -- 更新时间
    ext1            VARCHAR2(500),                      -- 扩展字段1
    ext2            VARCHAR2(500),                      -- 扩展字段2
    ext3            VARCHAR2(500)                       -- 扩展字段3
);

-- 创建索引
CREATE INDEX idx_tr_disch_visit_date   ON tr_disch_settle_visit(stat_date);
CREATE INDEX idx_tr_disch_visit_vst    ON tr_disch_settle_visit(visit_id);
CREATE UNIQUE INDEX uk_tr_settle_rcpt  ON tr_settle_master(rcpt_no);
CREATE INDEX idx_tr_settle_date        ON tr_settle_master(stat_date);
CREATE INDEX idx_tr_settle_vst         ON tr_settle_master(visit_id);
CREATE INDEX idx_tr_settle_pay_date    ON tr_settle_payments(stat_date);
CREATE INDEX idx_tr_settle_pay_rcpt    ON tr_settle_payments(rcpt_no);

-- 添加注释
COMMENT ON TABLE tr_disch_settle_visit IS '出院结算-出院记录(SP_DischSettle_1028/pat_visit,按出院日期抽取)';
COMMENT ON COLUMN tr_disch_settle_visit.stat_date IS '统计日期(=trunc(DISCHARGE_DATE_TIME) 出院日期)';
COMMENT ON COLUMN tr_disch_settle_visit.settle_status IS '状态:1已出院结算 2出院未结算 3结算有余额 4结算欠费 -1删除中间态';
COMMENT ON COLUMN tr_disch_settle_visit.patient_id IS '患者ID';
COMMENT ON COLUMN tr_disch_settle_visit.visit_id IS '住院就诊ID';
COMMENT ON COLUMN tr_disch_settle_visit.discharge_date_time IS '出院时间';
COMMENT ON COLUMN tr_disch_settle_visit.total_costs IS '总费用';
COMMENT ON COLUMN tr_disch_settle_visit.total_payments IS '总预交金';
COMMENT ON TABLE tr_settle_master IS '出院结算-结算主表(SP_SettleMaster_1029/inp_settle_master,按结算日期抽取)';
COMMENT ON COLUMN tr_settle_master.stat_date IS '统计日期(=trunc(SETTLING_DATE) 结算日期)';
COMMENT ON COLUMN tr_settle_master.rcpt_no IS '结算收据号(每笔结算唯一)';
COMMENT ON COLUMN tr_settle_master.patient_id IS '患者ID';
COMMENT ON COLUMN tr_settle_master.visit_id IS '住院就诊ID';
COMMENT ON COLUMN tr_settle_master.settling_date IS '结算时间';
COMMENT ON COLUMN tr_settle_master.operator_no IS '操作员工号(渠道:9111=自助机,其余=窗口)';
COMMENT ON COLUMN tr_settle_master.charge_type IS '结算费别(职工医保/异地职工医保/居民医保/自费等)';
COMMENT ON COLUMN tr_settle_master.costs IS '费用';
COMMENT ON COLUMN tr_settle_master.charges IS '结算金额';
COMMENT ON COLUMN tr_settle_master.payments IS '预缴金额';
COMMENT ON TABLE tr_settle_payments IS '出院结算-支付方式表(SP_SettlePaymentsMoney_1030,按结算日期抽取)';
COMMENT ON COLUMN tr_settle_payments.stat_date IS '统计日期(=trunc(SETTLING_DATE) 结算日期)';
COMMENT ON COLUMN tr_settle_payments.rcpt_no IS '结算收据号(关联结算主表)';
COMMENT ON COLUMN tr_settle_payments.patient_id IS '患者ID';
COMMENT ON COLUMN tr_settle_payments.visit_id IS '住院就诊ID';
COMMENT ON COLUMN tr_settle_payments.settling_date IS '结算时间';
COMMENT ON COLUMN tr_settle_payments.money_type IS '支付方式(微信/支付宝/银行卡/现金等)';
COMMENT ON COLUMN tr_settle_payments.payment_amount IS '支付金额(收)';
COMMENT ON COLUMN tr_settle_payments.refunded_amount IS '退费金额(退)';
