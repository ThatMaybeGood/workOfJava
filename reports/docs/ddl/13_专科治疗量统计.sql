-- ============================================================
-- 专科治疗量统计
-- ============================================================

-- 清理已存在对象(如重建请先执行)

DROP TABLE tr_spec_treat_ov CASCADE CONSTRAINTS;

-- 13.1 专科治疗量统计(按科室维度，无独立概览表)
CREATE TABLE tr_spec_treat_ov (
    stat_date       DATE            NOT NULL,           -- 统计日期
    dept_code       VARCHAR2(10)    NOT NULL,           -- 科室代码
    dept_name       VARCHAR2(100)   NOT NULL,           -- 科室名称
    treatment_count NUMBER(10)      DEFAULT 0,          -- 治疗人次
    treatment_amount NUMBER(18,2),                      -- 治疗金额
    patient_count   NUMBER(10)      DEFAULT 0,          -- 患者人数
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500),                        -- 扩展字段3
    CONSTRAINT pk_tr_spec_treat_ov PRIMARY KEY (stat_date, dept_code)
);

-- 添加注释

COMMENT ON TABLE tr_spec_treat_ov IS '专科治疗量统计-科室明细(按科室维度统计专科治疗量)';
COMMENT ON TABLE tr_spec_treat_ov IS '专科治疗量统计-科室明细(按科室维度统计专科治疗量)';
COMMENT ON COLUMN tr_spec_treat_ov.stat_date IS '统计日期';
COMMENT ON COLUMN tr_spec_treat_ov.dept_code IS '科室代码';
COMMENT ON COLUMN tr_spec_treat_ov.dept_name IS '科室名称';
COMMENT ON COLUMN tr_spec_treat_ov.treatment_count IS '治疗人次';
COMMENT ON COLUMN tr_spec_treat_ov.treatment_amount IS '治疗金额';
COMMENT ON COLUMN tr_spec_treat_ov.patient_count IS '患者人数';
COMMENT ON COLUMN tr_spec_treat_ov.create_time IS '创建时间';
COMMENT ON COLUMN tr_spec_treat_ov.update_time IS '更新时间';
COMMENT ON COLUMN tr_spec_treat_ov.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_spec_treat_ov.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_spec_treat_ov.ext3 IS '扩展字段3';
