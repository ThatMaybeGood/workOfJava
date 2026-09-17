-- ============================================================
-- 门诊服务质量分析
-- ============================================================

-- 清理已存在对象(如重建请先执行)

DROP TABLE tr_svc_quality_cmpl CASCADE CONSTRAINTS;

DROP TABLE tr_svc_quality_prz CASCADE CONSTRAINTS;

-- 12.2 门诊服务质量投诉明细表(人工登记渠道；源库 yq_powersfp 的明细只查不落库)
CREATE TABLE tr_svc_quality_cmpl (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    complaint_time  DATE,                               -- 投诉时间
    dept_code       VARCHAR2(10),                       -- 科室代码（关联 TR_DEPT_DICT）
    dept_name       VARCHAR2(100),                      -- 科室
    person_name     VARCHAR2(100),                      -- 人员
    position        VARCHAR2(50),                       -- 职位
    category        VARCHAR2(100),                      -- 分类
    result          VARCHAR2(200),                      -- 处理结果
    remark          VARCHAR2(500),                      -- 备注
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 12.3 门诊服务质量表扬明细表(同 12.2)
CREATE TABLE tr_svc_quality_prz (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    praise_time     DATE,                               -- 表扬时间
    dept_code       VARCHAR2(10),                       -- 科室代码（关联 TR_DEPT_DICT）
    dept_name       VARCHAR2(100),                      -- 科室
    person_name     VARCHAR2(100),                      -- 人员
    position        VARCHAR2(50),                       -- 职位
    method          VARCHAR2(100),                      -- 表扬方式
    feedback        VARCHAR2(500),                      -- 是否反馈科室(字典 feedback)；反馈内容来自源库答卷，不落库
    remark          VARCHAR2(500),                      -- 备注
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);


COMMENT ON TABLE tr_svc_quality_cmpl IS '门诊服务质量分析-投诉明细';
COMMENT ON COLUMN tr_svc_quality_cmpl.id IS '主键ID';
COMMENT ON COLUMN tr_svc_quality_cmpl.stat_date IS '统计日期';
COMMENT ON COLUMN tr_svc_quality_cmpl.complaint_time IS '投诉时间';
COMMENT ON COLUMN tr_svc_quality_cmpl.dept_code IS '科室代码';
COMMENT ON COLUMN tr_svc_quality_cmpl.dept_name IS '科室';
COMMENT ON COLUMN tr_svc_quality_cmpl.person_name IS '人员';
COMMENT ON COLUMN tr_svc_quality_cmpl.position IS '职位';
COMMENT ON COLUMN tr_svc_quality_cmpl.category IS '分类';
COMMENT ON COLUMN tr_svc_quality_cmpl.result IS '处理结果';
COMMENT ON COLUMN tr_svc_quality_cmpl.remark IS '备注';
COMMENT ON COLUMN tr_svc_quality_cmpl.create_time IS '创建时间';
COMMENT ON COLUMN tr_svc_quality_cmpl.update_time IS '更新时间';
COMMENT ON COLUMN tr_svc_quality_cmpl.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_svc_quality_cmpl.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_svc_quality_cmpl.ext3 IS '扩展字段3';
COMMENT ON TABLE tr_svc_quality_prz IS '门诊服务质量分析-表扬明细';
COMMENT ON COLUMN tr_svc_quality_prz.id IS '主键ID';
COMMENT ON COLUMN tr_svc_quality_prz.stat_date IS '统计日期';
COMMENT ON COLUMN tr_svc_quality_prz.praise_time IS '表扬时间';
COMMENT ON COLUMN tr_svc_quality_prz.dept_code IS '科室代码';
COMMENT ON COLUMN tr_svc_quality_prz.dept_name IS '科室';
COMMENT ON COLUMN tr_svc_quality_prz.person_name IS '人员';
COMMENT ON COLUMN tr_svc_quality_prz.position IS '职位';
COMMENT ON COLUMN tr_svc_quality_prz.method IS '表扬方式';
COMMENT ON COLUMN tr_svc_quality_prz.feedback IS '是否反馈科室(字典feedback)';
COMMENT ON COLUMN tr_svc_quality_prz.remark IS '备注';
COMMENT ON COLUMN tr_svc_quality_prz.create_time IS '创建时间';
COMMENT ON COLUMN tr_svc_quality_prz.update_time IS '更新时间';
COMMENT ON COLUMN tr_svc_quality_prz.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_svc_quality_prz.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_svc_quality_prz.ext3 IS '扩展字段3';
