-- ============================================================
-- 门诊预约实时数据表(结构与TR_OUTP_FIN_CLINIC_MASTER一致)
-- ============================================================
-- 用途: 预测门诊量公式的"当日实时预约量"取数源——存放未来日期(当前约15天)的预约行,
-- 由用户按日覆盖抽数(从HIS预约表)。
-- 为何不直接放TR_OUTP_FIN_CLINIC_MASTER: 该表从HIS clinic_master抽取, 预约(indicator=1)
-- 次日会转为挂号主表(indicator=2), 同一就诊会变成两行; 若预置未来预约行到该表,
-- 次日抽取会与转来的indicator=2行重复计数。故未来预约单独存本表, 仅预测公式读取。
-- 行内容: APPOINTS_INDICATOR恒为1(预约表内容), STAT_DATE=就诊日(未来)。
-- 口径注: 预测公式只取未来段(dd/total/appoint三列), 本表历史段无意义可不维护。

-- 清理已存在对象(如重建请先执行)
DROP TABLE TR_OUTP_FIN_APPOINTS CASCADE CONSTRAINTS;

CREATE TABLE TR_OUTP_FIN_APPOINTS (
    STAT_DATE            DATE NOT NULL,               -- 就诊日(未来日期)
    PATIENT_ID           VARCHAR2(50),
    VISIT_DATE           DATE NOT NULL,
    RETURNED_DATE        DATE,
    IS_RETURN_TYPE       NUMBER(1),
    CLINIC_LABEL         VARCHAR2(100),
    VISIT_TIME_DESC      VARCHAR2(100),
    OPERATOR_NO          VARCHAR2(20),
    REGIST_FEE           NUMBER(18,4),
    CLINIC_FEE           NUMBER(18,4),
    CREATE_TIME          DATE DEFAULT SYSDATE,
    UPDATE_TIME          DATE DEFAULT SYSDATE,
    ETL_SRC              VARCHAR2(100),
    ETL_BATCH_NO         VARCHAR2(50),
    SOURCE_TYPE          VARCHAR2(20),
    REGISTRATION_STATUS  NUMBER(1),
    RETURNED_OPERATOR    VARCHAR2(20),
    RETURNED_OPERATOR_NO VARCHAR2(8),
    QUEUE_TYPE           VARCHAR2(10),
    REGISTERING_DATE     DATE,
    APPOINTS_INDICATOR   NUMBER(1) NOT NULL,          -- 恒为1(预约表内容)
    VISIT_NO             NUMBER(18) NOT NULL,
    PATIENT_NAME         VARCHAR2(20),
    ID_NO                VARCHAR2(18),
    QUEUE_NUM            NUMBER,
    DEPT_CODE            VARCHAR2(100),
    CONSTRAINT PK_OUTP_FIN_APPOINTS PRIMARY KEY (STAT_DATE, VISIT_NO)
);

COMMENT ON TABLE TR_OUTP_FIN_APPOINTS IS '门诊预约实时数据(未来约15天, 预测门诊量"当日实时预约量"取数源; 不并入CLINIC_MASTER避免与次日转来的挂号主表行重复计数)';
COMMENT ON COLUMN TR_OUTP_FIN_APPOINTS.STAT_DATE IS '就诊日(未来日期),预测按此日归期';
COMMENT ON COLUMN TR_OUTP_FIN_APPOINTS.APPOINTS_INDICATOR IS '预约表内容标识, 本表恒为1';
