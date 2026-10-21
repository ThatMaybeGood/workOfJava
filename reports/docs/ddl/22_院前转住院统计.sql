-- ============================================================
-- 院前转住院统计
-- ============================================================
-- 报表无中间结果表: 统计直查HIS outp_rcpt_master + outp_treat_rec_t(与出院结算人次统计同库),
-- 只统计本表维护的有效操作员; 人次=count(distinct patient_id)
-- 本文件仅含维护功能配置表

DROP TABLE tr_prehosp_oper CASCADE CONSTRAINTS;

CREATE TABLE tr_prehosp_oper (
    id          NUMBER(10)      NOT NULL,               -- 主键(共用seq_tr_reports)
    oper_no     VARCHAR2(50)    NOT NULL,               -- 操作员=HIS users.USER_ID(统计查询按此过滤)
    dbuser      VARCHAR2(50)    NOT NULL,               -- 登录ID=HIS users.DB_USER(维护页添加/显示)
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
COMMENT ON COLUMN tr_prehosp_oper.valid_flag IS '是否有效: 1有效 0无效(无效不下拉不统计)';
COMMENT ON COLUMN tr_prehosp_oper.create_time IS '创建时间';
COMMENT ON COLUMN tr_prehosp_oper.update_time IS '更新时间';
