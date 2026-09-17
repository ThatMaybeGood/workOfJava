-- ============================================================
-- 门诊管理质量控制
-- ============================================================

-- 清理已存在对象(如重建请先执行)

DROP TABLE tr_qc_dtl CASCADE CONSTRAINTS;

-- 9.1 门诊质量控制月度明细表（一月一行，分子分母各占一列，比率取数时由SQL现算不落库）
CREATE TABLE tr_qc_dtl (
    stat_month                  VARCHAR2(7)     NOT NULL,   -- 统计月份(YYYY-MM)
    emr_usage_rate_num          NUMBER(18,4),               -- 门诊电子病历使用率-分子
    emr_usage_rate_den          NUMBER(18,4),               -- 门诊电子病历使用率-分母
    standard_diagnosis_rate_num NUMBER(18,4),               -- 门诊标准诊断使用率-分子
    standard_diagnosis_rate_den NUMBER(18,4),               -- 门诊标准诊断使用率-分母
    on_time_rate_num            NUMBER(18,4),               -- 门诊准时出诊率-分子
    on_time_rate_den            NUMBER(18,4),               -- 门诊准时出诊率-分母
    stop_rate_num               NUMBER(18,4),               -- 门诊停诊率-分子
    stop_rate_den               NUMBER(18,4),               -- 门诊停诊率-分母
    chemo_record_rate_num       NUMBER(18,4),               -- 门诊化疗病历记录完整率-分子
    chemo_record_rate_den       NUMBER(18,4),               -- 门诊化疗病历记录完整率-分母
    chemo_adverse_rate_num      NUMBER(18,4),               -- 门诊化疗严重不良反应发生率-分子
    chemo_adverse_rate_den      NUMBER(18,4),               -- 门诊化疗严重不良反应发生率-分母
    chemo_infusion_rate_num     NUMBER(18,4),               -- 门诊化疗患者静脉治疗相关不良事件发生率-分子
    chemo_infusion_rate_den     NUMBER(18,4),               -- 门诊化疗患者静脉治疗相关不良事件发生率-分母
    critical_value_rate_num     NUMBER(18,4),               -- 门诊危急值30分钟内通报完成率-分子
    critical_value_rate_den     NUMBER(18,4),               -- 门诊危急值30分钟内通报完成率-分母
    blood_draw_error_rate_num   NUMBER(18,4),               -- 门诊静脉采血相关差错发生率-分子
    blood_draw_error_rate_den   NUMBER(18,4),               -- 门诊静脉采血相关差错发生率-分母
    surgery_complication_rate_num NUMBER(18,4),             -- 门诊手术并发症发生率-分子
    surgery_complication_rate_den NUMBER(18,4),             -- 门诊手术并发症发生率-分母
    adverse_event_rate_num      NUMBER(18,4),               -- 每千门诊诊疗人次不良事件发生率-分子
    adverse_event_rate_den      NUMBER(18,4),               -- 每千门诊诊疗人次不良事件发生率-分母
    source_type                 VARCHAR2(20)    DEFAULT 'ETL抽取', -- 数据来源(最后一次写入的)
    create_time                 DATE            DEFAULT SYSDATE,
    update_time                 DATE            DEFAULT SYSDATE,
    ext1                        VARCHAR2(500),
    ext2                        VARCHAR2(500),
    ext3                        VARCHAR2(500),
    CONSTRAINT pk_tr_qc_dtl PRIMARY KEY (stat_month)
);

-- 添加注释
COMMENT ON TABLE tr_qc_dtl IS '门诊管理质量控制-月度明细(一月一行,按分子分母取数时现算比率)';
COMMENT ON COLUMN tr_qc_dtl.stat_month IS '统计月份(YYYY-MM)';
COMMENT ON COLUMN tr_qc_dtl.source_type IS '数据来源(人工登记/ETL抽取)，同一行由两边混合写入时记最后一次';
