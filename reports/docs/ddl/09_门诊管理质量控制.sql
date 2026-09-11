-- ============================================================
-- 门诊管理质量控制
-- ============================================================

-- 清理已存在对象(如重建请先执行)

DROP TABLE tr_qc_ov CASCADE CONSTRAINTS;

DROP TABLE tr_qc_dtl CASCADE CONSTRAINTS;

-- 9.1 门诊质量控制概览表
CREATE TABLE tr_qc_ov (
    id                  NUMBER(19)      PRIMARY KEY,
    stat_date           DATE            NOT NULL,       -- 统计日期
    emr_usage_rate      VARCHAR2(20),                  -- 病历使用率
    standard_diagnosis_rate VARCHAR2(20),              -- 规范诊断率
    on_time_rate        VARCHAR2(20),                  -- 准时率
    stop_rate           VARCHAR2(20),                  -- 停诊率
    chemo_record_rate   VARCHAR2(20),                  -- 化疗记录率
    chemo_adverse_rate  VARCHAR2(20),                  -- 化疗不良反应率
    chemo_infusion_rate VARCHAR2(20),                  -- 化疗输液率
    critical_value_rate VARCHAR2(20),                  -- 危急值处理率
    blood_draw_error_rate VARCHAR2(20),                -- 抽血差错率
    surgery_complication_rate VARCHAR2(20),            -- 手术并发症率
    adverse_event_rate  VARCHAR2(20),                  -- 不良事件率
    create_time         DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time         DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1                VARCHAR2(500),                        -- 扩展字段1
    ext2                VARCHAR2(500),                        -- 扩展字段2
    ext3                VARCHAR2(500)                         -- 扩展字段3
);

-- 9.2 门诊质量控制月度明细表
CREATE TABLE tr_qc_dtl (
    id                  NUMBER(19)      PRIMARY KEY,
    stat_month          VARCHAR2(20)    NOT NULL,       -- 统计月份(YYYY-MM)
    emr_usage_rate      VARCHAR2(20),                  -- 病历使用率
    standard_diagnosis_rate VARCHAR2(20),              -- 规范诊断率
    on_time_rate        VARCHAR2(20),                  -- 准时率
    stop_rate           VARCHAR2(20),                  -- 停诊率
    chemo_record_rate   VARCHAR2(20),                  -- 化疗记录率
    chemo_adverse_rate  VARCHAR2(20),                  -- 化疗不良反应率
    chemo_infusion_rate VARCHAR2(20),                  -- 化疗输液率
    critical_value_rate VARCHAR2(20),                  -- 危急值处理率
    blood_draw_error_rate VARCHAR2(20),                -- 抽血差错率
    surgery_complication_rate VARCHAR2(20),            -- 手术并发症率
    adverse_event_rate  VARCHAR2(20),                  -- 不良事件率
    create_time         DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time         DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1                VARCHAR2(500),                        -- 扩展字段1
    ext2                VARCHAR2(500),                        -- 扩展字段2
    ext3                VARCHAR2(500)                         -- 扩展字段3
);

-- 创建索引

CREATE INDEX idx_tr_qc_overview_date ON tr_qc_ov(stat_date);

-- 添加注释

COMMENT ON TABLE tr_qc_ov IS '门诊管理质量控制-概览';
COMMENT ON TABLE tr_qc_ov IS '门诊管理质量控制-概览(门诊质量管理各项质控指标的总览数据)';
COMMENT ON COLUMN tr_qc_ov.id IS '主键ID';
COMMENT ON COLUMN tr_qc_ov.stat_date IS '统计日期';
COMMENT ON COLUMN tr_qc_ov.emr_usage_rate IS '病历使用率';
COMMENT ON COLUMN tr_qc_ov.standard_diagnosis_rate IS '规范诊断率';
COMMENT ON COLUMN tr_qc_ov.on_time_rate IS '准时率';
COMMENT ON COLUMN tr_qc_ov.stop_rate IS '停诊率';
COMMENT ON COLUMN tr_qc_ov.chemo_record_rate IS '化疗记录率';
COMMENT ON COLUMN tr_qc_ov.chemo_adverse_rate IS '化疗不良反应率';
COMMENT ON COLUMN tr_qc_ov.chemo_infusion_rate IS '化疗输液率';
COMMENT ON COLUMN tr_qc_ov.critical_value_rate IS '危急值处理率';
COMMENT ON COLUMN tr_qc_ov.blood_draw_error_rate IS '抽血差错率';
COMMENT ON COLUMN tr_qc_ov.surgery_complication_rate IS '手术并发症率';
COMMENT ON COLUMN tr_qc_ov.adverse_event_rate IS '不良事件率';
COMMENT ON COLUMN tr_qc_ov.create_time IS '创建时间';
COMMENT ON COLUMN tr_qc_ov.update_time IS '更新时间';
COMMENT ON COLUMN tr_qc_ov.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_qc_ov.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_qc_ov.ext3 IS '扩展字段3';
COMMENT ON TABLE tr_qc_dtl IS '门诊管理质量控制-月度明细';
COMMENT ON TABLE tr_qc_dtl IS '门诊管理质量控制-月度明细(门诊质控指标按月度的详细记录,用于趋势分析)';
COMMENT ON COLUMN tr_qc_dtl.id IS '主键ID';
COMMENT ON COLUMN tr_qc_dtl.stat_month IS '统计月份(YYYY-MM)';
COMMENT ON COLUMN tr_qc_dtl.emr_usage_rate IS '病历使用率';
COMMENT ON COLUMN tr_qc_dtl.standard_diagnosis_rate IS '规范诊断率';
COMMENT ON COLUMN tr_qc_dtl.on_time_rate IS '准时率';
COMMENT ON COLUMN tr_qc_dtl.stop_rate IS '停诊率';
COMMENT ON COLUMN tr_qc_dtl.chemo_record_rate IS '化疗记录率';
COMMENT ON COLUMN tr_qc_dtl.chemo_adverse_rate IS '化疗不良反应率';
COMMENT ON COLUMN tr_qc_dtl.chemo_infusion_rate IS '化疗输液率';
COMMENT ON COLUMN tr_qc_dtl.critical_value_rate IS '危急值处理率';
COMMENT ON COLUMN tr_qc_dtl.blood_draw_error_rate IS '抽血差错率';
COMMENT ON COLUMN tr_qc_dtl.surgery_complication_rate IS '手术并发症率';
COMMENT ON COLUMN tr_qc_dtl.adverse_event_rate IS '不良事件率';
COMMENT ON COLUMN tr_qc_dtl.create_time IS '创建时间';
COMMENT ON COLUMN tr_qc_dtl.update_time IS '更新时间';
COMMENT ON COLUMN tr_qc_dtl.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_qc_dtl.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_qc_dtl.ext3 IS '扩展字段3';
