-- ============================================================
-- 住院预交金统计
-- ============================================================

-- 清理已存在对象(如重建请先执行)

DROP TABLE tr_inpat_prepay_ov CASCADE CONSTRAINTS;

DROP TABLE tr_inpat_prepay_dtl CASCADE CONSTRAINTS;

DROP TABLE tr_inpat_prepay_cht CASCADE CONSTRAINTS;

DROP TABLE tr_inpat_prepay_rcpt CASCADE CONSTRAINTS;

-- 18.1 住院预交金交易流水表(对应HIS prepayment_rcpt,报表按transact_date实时聚合)
CREATE TABLE tr_inpat_prepay_rcpt (
    stat_date           DATE            NOT NULL,   -- 统计日期(=transact_date的年月日,查询按此字段匹配)
    patient_id          VARCHAR2(50)    NOT NULL,   -- 患者ID
    visit_id            VARCHAR2(50)    NOT NULL,   -- 住院就诊ID(一次住院)
    rcpt_no             VARCHAR2(50)    NOT NULL,   -- 预交金收据号(每笔交易唯一)
    transact_type       VARCHAR2(20)    NOT NULL,   -- 交易类型(结算=出院结算出账,其余=预交金缴存/退款等)
    amount              NUMBER(18,2)    NOT NULL,   -- 交易金额
    pay_way             VARCHAR2(20),               -- 支付方式(现金/银行卡/微信/支付宝等)
    transact_date       DATE            NOT NULL,   -- 交易时间
    operator_no         VARCHAR2(50),               -- 操作员工号
    refunded_rcpt_no    VARCHAR2(50),               -- 退款票据号(退款交易指向被退的原收据号)
    acct_no             VARCHAR2(50),               -- 结账号(结算交易关联的结算单号)
    create_time         DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time         DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1                VARCHAR2(500),                        -- 扩展字段1
    ext2                VARCHAR2(500),                        -- 扩展字段2
    ext3                VARCHAR2(500)                         -- 扩展字段3
);

-- 创建索引

CREATE UNIQUE INDEX uk_tr_prepay_rcpt_no ON tr_inpat_prepay_rcpt(rcpt_no);
CREATE INDEX idx_tr_prepay_rcpt_statdate ON tr_inpat_prepay_rcpt(stat_date);
CREATE INDEX idx_tr_prepay_rcpt_date ON tr_inpat_prepay_rcpt(transact_date);
CREATE INDEX idx_tr_prepay_rcpt_visit ON tr_inpat_prepay_rcpt(visit_id);
CREATE INDEX idx_tr_prepay_rcpt_patient ON tr_inpat_prepay_rcpt(patient_id);
CREATE INDEX idx_tr_prepay_rcpt_refund ON tr_inpat_prepay_rcpt(refunded_rcpt_no);

-- 添加注释

COMMENT ON TABLE tr_inpat_prepay_rcpt IS '住院预交金统计-交易流水源表(对应HIS prepayment_rcpt,报表按交易类型/日期实时聚合)';
COMMENT ON COLUMN tr_inpat_prepay_rcpt.patient_id IS '患者ID';
COMMENT ON COLUMN tr_inpat_prepay_rcpt.visit_id IS '住院就诊ID(一次住院)';
COMMENT ON COLUMN tr_inpat_prepay_rcpt.rcpt_no IS '预交金收据号(每笔交易唯一)';
COMMENT ON COLUMN tr_inpat_prepay_rcpt.transact_type IS '交易类型(结算=出院结算出账,其余=预交金缴存/退款等)';
COMMENT ON COLUMN tr_inpat_prepay_rcpt.amount IS '交易金额';
COMMENT ON COLUMN tr_inpat_prepay_rcpt.pay_way IS '支付方式(现金/银行卡/微信/支付宝等)';
COMMENT ON COLUMN tr_inpat_prepay_rcpt.transact_date IS '交易时间';
COMMENT ON COLUMN tr_inpat_prepay_rcpt.stat_date IS '统计日期(=transact_date的年月日,查询按此字段匹配)';
COMMENT ON COLUMN tr_inpat_prepay_rcpt.operator_no IS '操作员工号';
COMMENT ON COLUMN tr_inpat_prepay_rcpt.refunded_rcpt_no IS '退款票据号(退款交易指向被退的原收据号)';
COMMENT ON COLUMN tr_inpat_prepay_rcpt.acct_no IS '结账号(结算交易关联的结算单号)';
COMMENT ON COLUMN tr_inpat_prepay_rcpt.create_time IS '创建时间';
COMMENT ON COLUMN tr_inpat_prepay_rcpt.update_time IS '更新时间';
COMMENT ON COLUMN tr_inpat_prepay_rcpt.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_inpat_prepay_rcpt.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_inpat_prepay_rcpt.ext3 IS '扩展字段3';
