-- ============================================================
-- 门诊管理质量控制
-- ============================================================

-- 清理已存在对象(如重建请先执行)

DROP TABLE tr_qc_ov CASCADE CONSTRAINTS;

DROP TABLE tr_qc_dtl CASCADE CONSTRAINTS;

DROP TABLE tr_qc_maintain CASCADE CONSTRAINTS;

-- 9.1 门诊质量控制概览表
CREATE TABLE tr_qc_ov (
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
-- 9.3 质控指标人工维护表（数据维护弹窗保存至此，SOURCE_TYPE 标记来源，报表查询时优先取人工登记值覆盖ETL值）
CREATE TABLE tr_qc_maintain (
    id              NUMBER(18)      NOT NULL,       -- 主键(seq_tr_reports)
    stat_month      VARCHAR2(7)     NOT NULL,       -- 统计月份(YYYY-MM)
    indicator_code  VARCHAR2(50)    NOT NULL,       -- 指标编码(对应tr_qc_dtl列名)
    indicator_name  VARCHAR2(100),                  -- 指标名称
    numerator       NUMBER(18,4),                   -- 分子值
    denominator     NUMBER(18,4),                   -- 分母值
    rate            NUMBER(10,4),                   -- 比率(分子/分母*100)
    source_type     VARCHAR2(20)    DEFAULT '人工登记', -- 数据来源
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- 创建索引（不唯一：同月同指标允许 ETL抽取/人工登记 并存，查询按人工登记优先取）
CREATE INDEX idx_qc_maintain_month ON tr_qc_maintain(stat_month, indicator_code);

-- 添加注释
COMMENT ON TABLE tr_qc_maintain IS '门诊质控指标人工维护表';
COMMENT ON COLUMN tr_qc_maintain.stat_month IS '统计月份(YYYY-MM)';
COMMENT ON COLUMN tr_qc_maintain.indicator_code IS '指标编码(对应tr_qc_dtl列名)';
COMMENT ON COLUMN tr_qc_maintain.indicator_name IS '指标名称';
COMMENT ON COLUMN tr_qc_maintain.numerator IS '分子值';
COMMENT ON COLUMN tr_qc_maintain.denominator IS '分母值';
COMMENT ON COLUMN tr_qc_maintain.rate IS '比率(分子/分母*100)';
COMMENT ON COLUMN tr_qc_maintain.source_type IS '数据来源(人工登记/ETL抽取)，同月同指标可并存，报表取数人工登记优先';

COMMENT ON TABLE tr_qc_dtl IS '门诊管理质量控制-月度明细';
COMMENT ON TABLE tr_qc_dtl IS '门诊管理质量控制-月度明细(门诊质控指标按月度的详细记录,用于趋势分析)';
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
