-- ============================================================
-- 患者画像
-- ============================================================

-- 清理已存在对象(如重建请先执行)

DROP TABLE tr_pat_portrait_age CASCADE CONSTRAINTS;

DROP TABLE tr_pat_portrait_insur CASCADE CONSTRAINTS;

DROP TABLE tr_pat_portrait_idty CASCADE CONSTRAINTS;

DROP TABLE tr_pat_portrait_reg CASCADE CONSTRAINTS;

DROP TABLE tr_pat_portrait_arc CASCADE CONSTRAINTS;

-- 8.1 患者画像年龄分析表
CREATE TABLE tr_pat_portrait_age (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    dept_code       VARCHAR2(50),                       -- 科室编码
    patient_type    VARCHAR2(20),                       -- 患者类型(outpatient门诊/inpatient住院)
    age_group       VARCHAR2(50)    NOT NULL,           -- 年龄段
    archive_count   NUMBER(10)      DEFAULT 0,          -- 建档人数
    outpatient_count NUMBER(10)     DEFAULT 0,          -- 门诊人数
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 8.2 患者画像医保分析表
CREATE TABLE tr_pat_portrait_insur (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    dept_code       VARCHAR2(50),                       -- 科室编码
    patient_type    VARCHAR2(20),                       -- 患者类型(outpatient门诊/inpatient住院)
    insurance_name  VARCHAR2(100)   NOT NULL,           -- 医保名称
    patient_count   NUMBER(10)      DEFAULT 0,          -- 患者人数
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 8.3 患者画像身份分析表
CREATE TABLE tr_pat_portrait_idty (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    dept_code       VARCHAR2(50),                       -- 科室编码
    patient_type    VARCHAR2(20),                       -- 患者类型(outpatient门诊/inpatient住院)
    identity_name   VARCHAR2(100)   NOT NULL,           -- 身份名称
    patient_count   NUMBER(10)      DEFAULT 0,          -- 患者人数
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 8.4 患者画像挂号来源分析表
CREATE TABLE tr_pat_portrait_reg (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    dept_code       VARCHAR2(50),                       -- 科室编码
    patient_type    VARCHAR2(20),                       -- 患者类型(outpatient门诊/inpatient住院)
    source_name     VARCHAR2(100)   NOT NULL,           -- 来源名称
    patient_count   NUMBER(10)      DEFAULT 0,          -- 患者人数
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 8.5 患者画像建档来源分析表
CREATE TABLE tr_pat_portrait_arc (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    dept_code       VARCHAR2(50),                       -- 科室编码
    patient_type    VARCHAR2(20),                       -- 患者类型(outpatient门诊/inpatient住院)
    source_name     VARCHAR2(100)   NOT NULL,           -- 来源名称
    patient_count   NUMBER(10)      DEFAULT 0,          -- 患者人数
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 创建索引

CREATE INDEX idx_tr_portrait_age_date ON tr_pat_portrait_age(stat_date);

-- 添加注释

COMMENT ON TABLE tr_pat_portrait_age IS '患者画像-年龄分析';
COMMENT ON TABLE tr_pat_portrait_age IS '患者画像-年龄分析(按年龄段分析患者建档和门诊就诊分布)';
COMMENT ON COLUMN tr_pat_portrait_age.id IS '主键ID';
COMMENT ON COLUMN tr_pat_portrait_age.stat_date IS '统计日期';
COMMENT ON COLUMN tr_pat_portrait_age.age_group IS '年龄段';
COMMENT ON COLUMN tr_pat_portrait_age.archive_count IS '建档人数';
COMMENT ON COLUMN tr_pat_portrait_age.outpatient_count IS '门诊人数';
COMMENT ON COLUMN tr_pat_portrait_age.create_time IS '创建时间';
COMMENT ON COLUMN tr_pat_portrait_age.update_time IS '更新时间';
COMMENT ON COLUMN tr_pat_portrait_age.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_pat_portrait_age.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_pat_portrait_age.ext3 IS '扩展字段3';
COMMENT ON COLUMN TR_PAT_PORTRAIT_AGE.patient_type IS '患者类型：outpatient（门诊患者）、inpatient（住院患者）';
COMMENT ON COLUMN TR_PAT_PORTRAIT_AGE.dept_code IS '科室编码';
COMMENT ON COLUMN TR_PAT_PORTRAIT_AGE.dept_name IS '科室名称';
COMMENT ON TABLE tr_pat_portrait_insur IS '患者画像-医保分析';
COMMENT ON TABLE tr_pat_portrait_insur IS '患者画像-医保分析(按医保类型分析患者分布)';
COMMENT ON COLUMN tr_pat_portrait_insur.id IS '主键ID';
COMMENT ON COLUMN tr_pat_portrait_insur.stat_date IS '统计日期';
COMMENT ON COLUMN tr_pat_portrait_insur.insurance_name IS '医保名称';
COMMENT ON COLUMN tr_pat_portrait_insur.patient_count IS '患者人数';
COMMENT ON COLUMN tr_pat_portrait_insur.create_time IS '创建时间';
COMMENT ON COLUMN tr_pat_portrait_insur.update_time IS '更新时间';
COMMENT ON COLUMN tr_pat_portrait_insur.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_pat_portrait_insur.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_pat_portrait_insur.ext3 IS '扩展字段3';
COMMENT ON COLUMN TR_PAT_PORTRAIT_INSUR.patient_type IS '患者类型：outpatient（门诊患者）、inpatient（住院患者）';
COMMENT ON COLUMN TR_PAT_PORTRAIT_INSUR.dept_code IS '科室编码';
COMMENT ON COLUMN TR_PAT_PORTRAIT_INSUR.dept_name IS '科室名称';
COMMENT ON TABLE tr_pat_portrait_idty IS '患者画像-身份分析';
COMMENT ON TABLE tr_pat_portrait_idty IS '患者画像-身份分析(按患者身份类型分析分布)';
COMMENT ON COLUMN tr_pat_portrait_idty.id IS '主键ID';
COMMENT ON COLUMN tr_pat_portrait_idty.stat_date IS '统计日期';
COMMENT ON COLUMN tr_pat_portrait_idty.identity_name IS '身份名称';
COMMENT ON COLUMN tr_pat_portrait_idty.patient_count IS '患者人数';
COMMENT ON COLUMN tr_pat_portrait_idty.create_time IS '创建时间';
COMMENT ON COLUMN tr_pat_portrait_idty.update_time IS '更新时间';
COMMENT ON COLUMN tr_pat_portrait_idty.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_pat_portrait_idty.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_pat_portrait_idty.ext3 IS '扩展字段3';
COMMENT ON COLUMN TR_PAT_PORTRAIT_IDTY.patient_type IS '患者类型：outpatient（门诊患者）、inpatient（住院患者）';
COMMENT ON COLUMN TR_PAT_PORTRAIT_IDTY.dept_code IS '科室编码';
COMMENT ON COLUMN TR_PAT_PORTRAIT_IDTY.dept_name IS '科室名称';
COMMENT ON TABLE tr_pat_portrait_reg IS '患者画像-挂号来源';
COMMENT ON TABLE tr_pat_portrait_reg IS '患者画像-挂号来源(按挂号来源分析患者分布)';
COMMENT ON COLUMN tr_pat_portrait_reg.id IS '主键ID';
COMMENT ON COLUMN tr_pat_portrait_reg.stat_date IS '统计日期';
COMMENT ON COLUMN tr_pat_portrait_reg.source_name IS '来源名称';
COMMENT ON COLUMN tr_pat_portrait_reg.patient_count IS '患者人数';
COMMENT ON COLUMN tr_pat_portrait_reg.create_time IS '创建时间';
COMMENT ON COLUMN tr_pat_portrait_reg.update_time IS '更新时间';
COMMENT ON COLUMN tr_pat_portrait_reg.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_pat_portrait_reg.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_pat_portrait_reg.ext3 IS '扩展字段3';
COMMENT ON COLUMN TR_PAT_PORTRAIT_REG.patient_type IS '患者类型：outpatient（门诊患者）、inpatient（住院患者）';
COMMENT ON COLUMN TR_PAT_PORTRAIT_REG.dept_code IS '科室编码';
COMMENT ON COLUMN TR_PAT_PORTRAIT_REG.dept_name IS '科室名称';
COMMENT ON TABLE tr_pat_portrait_arc IS '患者画像-建档来源';
COMMENT ON TABLE tr_pat_portrait_arc IS '患者画像-建档来源(按建档来源分析患者分布)';
COMMENT ON COLUMN tr_pat_portrait_arc.id IS '主键ID';
COMMENT ON COLUMN tr_pat_portrait_arc.stat_date IS '统计日期';
COMMENT ON COLUMN tr_pat_portrait_arc.source_name IS '来源名称';
COMMENT ON COLUMN tr_pat_portrait_arc.patient_count IS '患者人数';
COMMENT ON COLUMN tr_pat_portrait_arc.create_time IS '创建时间';
COMMENT ON COLUMN tr_pat_portrait_arc.update_time IS '更新时间';
COMMENT ON COLUMN tr_pat_portrait_arc.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_pat_portrait_arc.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_pat_portrait_arc.ext3 IS '扩展字段3';
COMMENT ON COLUMN TR_PAT_PORTRAIT_ARC.patient_type IS '患者类型：outpatient（门诊患者）、inpatient（住院患者）';
COMMENT ON COLUMN TR_PAT_PORTRAIT_ARC.dept_code IS '科室编码';
COMMENT ON COLUMN TR_PAT_PORTRAIT_ARC.dept_name IS '科室名称';
