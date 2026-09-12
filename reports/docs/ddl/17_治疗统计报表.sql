-- ============================================================
-- 治疗统计报表
-- ============================================================

-- 清理已存在对象(如重建请先执行)

DROP TABLE tr_treat_stat_ov CASCADE CONSTRAINTS;

DROP TABLE tr_treat_stat_dtl CASCADE CONSTRAINTS;

DROP TABLE tr_treat_stat_trend CASCADE CONSTRAINTS;

-- 17.1 治疗统计概览表
CREATE TABLE tr_treat_stat_ov (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    patient_count   NUMBER(10)      DEFAULT 0,          -- 患者人数
    treatment_count NUMBER(10)      DEFAULT 0,          -- 治疗人次
    treatment_amount NUMBER(18,2),                      -- 治疗金额
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 17.2 治疗统计科室明细表
-- 注意: 不设 dept_name 列。
--   TreatmentStatsMapper.queryDeptDetail 在 JOIN TR_DEPT_DICT 时使用未限定的 dept_name,
--   若本表也存在 dept_name 会触发 ORA-00918(列名不明确), 故科室名称一律取自 TR_DEPT_DICT。
CREATE TABLE tr_treat_stat_dtl (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    dept_code       VARCHAR2(50),                       -- 科室编码(与 TR_DEPT_DICT 关联)
    patient_count   NUMBER(10)      DEFAULT 0,          -- 患者人数
    treatment_count NUMBER(10)      DEFAULT 0,          -- 治疗人次
    treatment_amount NUMBER(18,2),                      -- 治疗金额
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 17.3 治疗统计每日趋势表
CREATE TABLE tr_treat_stat_trend (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    trend_date      DATE            NOT NULL,           -- 趋势日期
    trend_value     NUMBER(10)      DEFAULT 0,          -- 趋势值
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 创建索引

CREATE INDEX idx_tr_treat_overview_date ON tr_treat_stat_ov(stat_date);
CREATE INDEX idx_tr_treat_detail_date ON tr_treat_stat_dtl(stat_date);
CREATE UNIQUE INDEX uk_tr_treat_stat_dtl ON tr_treat_stat_dtl(stat_date, dept_code);
CREATE INDEX idx_tr_treat_trend_date ON tr_treat_stat_trend(stat_date);

-- 添加注释

COMMENT ON TABLE tr_treat_stat_ov IS '治疗统计报表-概览(存储治疗统计总览:患者人数、治疗人次、治疗金额)';
COMMENT ON COLUMN tr_treat_stat_ov.id IS '主键ID';
COMMENT ON COLUMN tr_treat_stat_ov.stat_date IS '统计日期';
COMMENT ON COLUMN tr_treat_stat_ov.patient_count IS '患者人数';
COMMENT ON COLUMN tr_treat_stat_ov.treatment_count IS '治疗人次';
COMMENT ON COLUMN tr_treat_stat_ov.treatment_amount IS '治疗金额';
COMMENT ON COLUMN tr_treat_stat_ov.create_time IS '创建时间';
COMMENT ON COLUMN tr_treat_stat_ov.update_time IS '更新时间';
COMMENT ON COLUMN tr_treat_stat_ov.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_treat_stat_ov.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_treat_stat_ov.ext3 IS '扩展字段3';
COMMENT ON TABLE tr_treat_stat_dtl IS '治疗统计报表-科室明细(按科室维度统计治疗人次、金额、患者人数)';
COMMENT ON COLUMN tr_treat_stat_dtl.id IS '主键ID';
COMMENT ON COLUMN tr_treat_stat_dtl.stat_date IS '统计日期';
COMMENT ON COLUMN tr_treat_stat_dtl.dept_code IS '科室编码(关联TR_DEPT_DICT)';
COMMENT ON COLUMN tr_treat_stat_dtl.patient_count IS '患者人数';
COMMENT ON COLUMN tr_treat_stat_dtl.treatment_count IS '治疗人次';
COMMENT ON COLUMN tr_treat_stat_dtl.treatment_amount IS '治疗金额';
COMMENT ON COLUMN tr_treat_stat_dtl.create_time IS '创建时间';
COMMENT ON COLUMN tr_treat_stat_dtl.update_time IS '更新时间';
COMMENT ON COLUMN tr_treat_stat_dtl.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_treat_stat_dtl.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_treat_stat_dtl.ext3 IS '扩展字段3';
COMMENT ON TABLE tr_treat_stat_trend IS '治疗统计报表-每日趋势(存储每日治疗量趋势数据,用于趋势图展示)';
COMMENT ON COLUMN tr_treat_stat_trend.id IS '主键ID';
COMMENT ON COLUMN tr_treat_stat_trend.stat_date IS '统计日期';
COMMENT ON COLUMN tr_treat_stat_trend.trend_date IS '趋势日期';
COMMENT ON COLUMN tr_treat_stat_trend.trend_value IS '趋势值';
COMMENT ON COLUMN tr_treat_stat_trend.create_time IS '创建时间';
COMMENT ON COLUMN tr_treat_stat_trend.update_time IS '更新时间';
COMMENT ON COLUMN tr_treat_stat_trend.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_treat_stat_trend.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_treat_stat_trend.ext3 IS '扩展字段3';
