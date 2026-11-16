-- ============================================================
-- 出院结算人次统计
-- ============================================================
-- 现状: 报表直查HIS inp_settle_master+inp_acct_master+users(@DataSource("his"), 口径见mapper XML)
-- 本文件提供中间表模式(可选): 开关 reports.his-mid-table.disch-settle-cnt=true 时,
-- 报表改查本表 tr_disch_settle_cnt_d, 数据由HIS侧SP按日/按范围灌入(docs/his-sp/SP_DISCH_SETTLE_CNT_1116.sql)
-- 灌入方式自定(抽取过程不在本仓库), 约定灌入前先删覆盖范围内已有stat_date再插, 可重复执行;
-- 表不带DROP: 重复执行靠建表已存在报错(ORA-00955)跳过, 不会清掉存量数据
-- 口径等价: 行粒度保留(charge_type/操作员姓名存RAWTOHEX原文由Java解GBK), 账户发票数由SP抽取时
-- 关联inp_acct_master挂在结算行上(无匹配为NULL, 报表SUM计0), 发票去重分区口径不变

-- 25.1 结算行明细表(SP_DISCH_SETTLE_CNT_1116: inp_settle_master+users+inp_acct_master, 按结算日)
CREATE TABLE tr_disch_settle_cnt_d (
    stat_date             DATE NOT NULL,                -- 统计日期(=trunc(SETTLING_DATE) 结算日期)
    rcpt_no               VARCHAR2(50) NOT NULL,        -- 结算收据号(每笔结算唯一)
    acct_no               VARCHAR2(50),                 -- 结算账户号(发票去重分区字段)
    operator_no           VARCHAR2(50),                 -- 操作员工号(渠道: E700/9111=自助机)
    charge_type_hex       VARCHAR2(200),                -- 费别RAWTOHEX原文(GBK hex, Java解码)
    patient_id            VARCHAR2(50),                 -- 患者ID
    visit_id              VARCHAR2(50),                 -- 住院就诊ID
    settling_date         DATE,                         -- 结算时间(发票去重排序字段, 原值保留)
    operator_name_hex     VARCHAR2(200),                -- 操作员姓名RAWTOHEX原文(users无记录时=工号hex)
    invoice_printed_total NUMBER(10),                   -- 自助结算打印发票数(关联inp_acct_master, 无匹配为NULL计0)
    create_time           DATE DEFAULT SYSDATE,         -- 创建时间
    update_time           DATE DEFAULT SYSDATE,         -- 更新时间
    CONSTRAINT pk_tr_disch_settle_cnt_d PRIMARY KEY (stat_date, rcpt_no)
);

COMMENT ON TABLE tr_disch_settle_cnt_d IS '出院结算人次统计-结算行明细(SP_DISCH_SETTLE_CNT_1116/inp_settle_master,按结算日; 开关reports.his-mid-table.disch-settle-cnt)';
COMMENT ON COLUMN tr_disch_settle_cnt_d.stat_date IS '统计日期(=trunc(SETTLING_DATE) 结算日期)';
COMMENT ON COLUMN tr_disch_settle_cnt_d.rcpt_no IS '结算收据号(每笔结算唯一)';
COMMENT ON COLUMN tr_disch_settle_cnt_d.acct_no IS '结算账户号(发票去重分区字段)';
COMMENT ON COLUMN tr_disch_settle_cnt_d.operator_no IS '操作员工号(渠道: E700/9111=自助机, 其余窗口)';
COMMENT ON COLUMN tr_disch_settle_cnt_d.charge_type_hex IS '费别RAWTOHEX原文(HIS为US7ASCII存GBK字节, hex纯ASCII由Java解GBK)';
COMMENT ON COLUMN tr_disch_settle_cnt_d.patient_id IS '患者ID';
COMMENT ON COLUMN tr_disch_settle_cnt_d.visit_id IS '住院就诊ID';
COMMENT ON COLUMN tr_disch_settle_cnt_d.settling_date IS '结算时间(原值, 发票去重ROW_NUMBER排序用)';
COMMENT ON COLUMN tr_disch_settle_cnt_d.operator_name_hex IS '操作员姓名RAWTOHEX原文(users无记录时=工号hex)';
COMMENT ON COLUMN tr_disch_settle_cnt_d.invoice_printed_total IS '自助结算打印发票数(SP关联inp_acct_master, 无匹配为NULL, 报表SUM计0)';

-- 验证: 应返回1行
SELECT table_name FROM user_tables WHERE table_name = 'TR_DISCH_SETTLE_CNT_D';

-- 【日常维护】
-- 重抽某天: DELETE FROM tr_disch_settle_cnt_d WHERE stat_date = DATE '2026-xx-xx';
-- 覆盖范围检查: SELECT MIN(stat_date), MAX(stat_date), COUNT(*) FROM tr_disch_settle_cnt_d;
