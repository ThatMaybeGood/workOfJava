-- ============================================================
-- 院前转住院统计
-- ============================================================
-- 报表无中间结果表: 统计直查HIS outp_rcpt_master + outp_treat_rec_t(与出院结算人次统计同库),
-- 统计对象为负交易(total_charges<0, 退费冲红), 负交易经refunded_rcpt_no找回被退正交易,
-- 正交易存在且outp_treat_rec_t PRE_HOSPITAL<=1才算院前(院前标记在正交易, 直接判正交易误收普通正交易);
-- 只统计本表维护的有效操作员; 收据张数/人次/操作员均取负交易行, 人次=count(distinct patient_id)
-- 本文件仅含维护功能配置表

DROP TABLE tr_prehosp_oper CASCADE CONSTRAINTS;

CREATE TABLE tr_prehosp_oper (
    id          NUMBER(10)      NOT NULL,               -- 主键(共用seq_tr_reports)
    oper_no     VARCHAR2(50)    NOT NULL,               -- 操作员=HIS users.USER_ID(统计查询按此过滤)
    dbuser      VARCHAR2(50)    NOT NULL,               -- 登录ID=HIS users.DB_USER(维护页添加/显示)
    oper_name   VARCHAR2(50),                           -- 操作员姓名(添加时从HIS users.USER_NAME回显落库, 统计/列表直读本列不再回HIS查)
    valid_flag  NUMBER(1)       DEFAULT 1,              -- 是否有效: 1有效 0无效(无效不下拉不统计)
    create_time DATE            DEFAULT SYSDATE,
    update_time DATE            DEFAULT SYSDATE,
    CONSTRAINT pk_tr_prehosp_oper PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uk_tr_prehosp_oper_no ON tr_prehosp_oper(oper_no);

COMMENT ON TABLE tr_prehosp_oper IS '院前转住院统计-院前操作员维护表(仅统计本表维护的有效操作员)';
COMMENT ON COLUMN tr_prehosp_oper.id IS '主键(共用seq_tr_reports)';
COMMENT ON COLUMN tr_prehosp_oper.oper_no IS '操作员=HIS users.USER_ID(统计按此过滤; 维护页添加时由登录ID+姓名选定)';
COMMENT ON COLUMN tr_prehosp_oper.dbuser IS '登录ID=HIS users.DB_USER(维护页添加/显示)';
COMMENT ON COLUMN tr_prehosp_oper.oper_name IS '操作员姓名(添加时从HIS users.USER_NAME落库)';
COMMENT ON COLUMN tr_prehosp_oper.valid_flag IS '是否有效: 1有效 0无效(无效不下拉不统计)';
COMMENT ON COLUMN tr_prehosp_oper.create_time IS '创建时间';
COMMENT ON COLUMN tr_prehosp_oper.update_time IS '更新时间';

-- ============================================================
-- 2026-11-16 v2: 统计中间表(可选, 开关 reports.his-mid-table.pre-hosp-trans)
-- ============================================================
-- 原直查HIS(outp_rcpt_master)逻辑不变, 开关false(默认)走原逻辑;
-- true时统计改查本表 tr_prehosp_trans_d(口径同原过滤: total_charges<0且refunded_rcpt_no非空,
-- SP抽取时已过滤, 报表SQL保留同条件双保险), 由HIS侧SP_PREHOSP_TRANS_1116灌入(docs/his-sp/,
-- 入参为起止日期范围, 单日抽取传start=end=当天, 批量历史一次性传大范围),
-- 行粒度=负交易收据, 人次=count(distinct patient_id)在报表侧实时算;
-- 灌入约定先删覆盖范围内已有stat_date再插; 表不带DROP, 重复执行靠建表已存在跳过
-- 注意: 维护页操作员搜索(searchUsers等)仍直查HIS users, 无日期维度, 不在本开关范围

-- 22.2 负交易收据明细表(SP_PREHOSP_TRANS_1116: outp_rcpt_master, 按就诊日, 负交易口径)
CREATE TABLE tr_prehosp_trans_d (
    stat_date        DATE NOT NULL,                     -- 统计日期(=trunc(VISIT_DATE) 就诊日期)
    rcpt_no          VARCHAR2(50) NOT NULL,             -- 收据号(每笔收据唯一)
    visit_date       DATE,                              -- 就诊时间(原值保留)
    operator_no      VARCHAR2(50),                      -- 操作员工号(报表按维护表有效操作员过滤)
    patient_id       VARCHAR2(50),                      -- 患者ID(人次=count distinct本列)
    total_charges    NUMBER(18,2),                      -- 总金额(负交易口径留痕, <0)
    refunded_rcpt_no VARCHAR2(50),                      -- 被退收据号(负交易口径留痕, 非空)
    create_time      DATE DEFAULT SYSDATE,              -- 创建时间
    update_time      DATE DEFAULT SYSDATE,              -- 更新时间
    CONSTRAINT pk_tr_prehosp_trans_d PRIMARY KEY (stat_date, rcpt_no)
);

COMMENT ON TABLE tr_prehosp_trans_d IS '院前转住院统计-负交易收据明细(SP_PREHOSP_TRANS_1116/outp_rcpt_master,按就诊日; 开关reports.his-mid-table.pre-hosp-trans)';
COMMENT ON COLUMN tr_prehosp_trans_d.stat_date IS '统计日期(=trunc(VISIT_DATE) 就诊日期)';
COMMENT ON COLUMN tr_prehosp_trans_d.rcpt_no IS '收据号(每笔收据唯一)';
COMMENT ON COLUMN tr_prehosp_trans_d.visit_date IS '就诊时间(原值)';
COMMENT ON COLUMN tr_prehosp_trans_d.operator_no IS '操作员工号(报表只统计维护表有效操作员)';
COMMENT ON COLUMN tr_prehosp_trans_d.patient_id IS '患者ID(人次=count distinct)';
COMMENT ON COLUMN tr_prehosp_trans_d.total_charges IS '总金额(抽取时已过滤<0, 本列留痕)';
COMMENT ON COLUMN tr_prehosp_trans_d.refunded_rcpt_no IS '被退收据号(抽取时已过滤非空, 本列留痕)';

-- 验证: 应返回1行
SELECT table_name FROM user_tables WHERE table_name = 'TR_PREHOSP_TRANS_D';

-- 【日常维护】
-- 重抽某天: DELETE FROM tr_prehosp_trans_d WHERE stat_date = DATE '2026-xx-xx';
-- 覆盖范围检查: SELECT MIN(stat_date), MAX(stat_date), COUNT(*) FROM tr_prehosp_trans_d;
