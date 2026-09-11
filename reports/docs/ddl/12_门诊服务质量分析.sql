-- ============================================================
-- 门诊服务质量分析
-- ============================================================

-- 清理已存在对象(如重建请先执行)

DROP TABLE tr_svc_quality_cmpl CASCADE CONSTRAINTS;

DROP TABLE tr_svc_quality_prz CASCADE CONSTRAINTS;

DROP TABLE TR_COMMON_DICT CASCADE CONSTRAINTS;

DROP TABLE TR_STAFF_DICT CASCADE CONSTRAINTS;

-- 12.2 门诊服务质量投诉明细表
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

-- 12.3 门诊服务质量表扬明细表
CREATE TABLE tr_svc_quality_prz (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    praise_time     DATE,                               -- 表扬时间
    dept_code       VARCHAR2(10),                       -- 科室代码（关联 TR_DEPT_DICT）
    dept_name       VARCHAR2(100),                      -- 科室
    person_name     VARCHAR2(100),                      -- 人员
    position        VARCHAR2(50),                       -- 职位
    method          VARCHAR2(100),                      -- 表扬方式
    feedback        VARCHAR2(500),                      -- 反馈内容
    remark          VARCHAR2(500),                      -- 备注
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 12.4 通用字典表（岗位类别/投诉分类/投诉处理结果/表扬方式/是否反馈科室等）
CREATE TABLE tr_common_dict (
    id          NUMBER(19)      PRIMARY KEY,
    dict_type   VARCHAR2(50)    NOT NULL,           -- 字典类型：position/complaintCategory/complaintResult/praiseMethod/feedback
    dict_code   VARCHAR2(50)    NOT NULL,           -- 字典编码
    dict_name   VARCHAR2(200)   NOT NULL,           -- 字典名称
    sort_no     NUMBER(6)       DEFAULT 0,          -- 排序号
    status      NUMBER(1)       DEFAULT 1,          -- 状态：1启用 0停用
    create_time DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1        VARCHAR2(500),                        -- 扩展字段1
    ext2        VARCHAR2(500),                        -- 扩展字段2
    ext3        VARCHAR2(500)                         -- 扩展字段3
);

-- 12.5 人员字典表
CREATE TABLE tr_staff_dict (
    id          NUMBER(19)      PRIMARY KEY,
    staff_code  VARCHAR2(50)    NOT NULL,           -- 人员工号
    staff_name  VARCHAR2(100)   NOT NULL,           -- 人员姓名
    dept_code   VARCHAR2(10),                       -- 所属科室代码（关联 TR_DEPT_DICT）
    dept_name   VARCHAR2(100),                      -- 所属科室名称
    position    VARCHAR2(50),                       -- 岗位类别
    status      NUMBER(1)       DEFAULT 1,          -- 状态：1在职 0停用
    create_time DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1        VARCHAR2(500),                        -- 扩展字段1
    ext2        VARCHAR2(500),                        -- 扩展字段2
    ext3        VARCHAR2(500)                         -- 扩展字段3
);

-- 创建索引

-- 添加注释

COMMENT ON TABLE tr_svc_quality_cmpl IS '门诊服务质量分析-投诉明细';
COMMENT ON TABLE tr_svc_quality_cmpl IS '门诊服务质量分析-投诉明细(门诊投诉事件的详细记录)';
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
COMMENT ON TABLE tr_svc_quality_prz IS '门诊服务质量分析-表扬明细(门诊表扬事件的详细记录)';
COMMENT ON COLUMN tr_svc_quality_prz.id IS '主键ID';
COMMENT ON COLUMN tr_svc_quality_prz.stat_date IS '统计日期';
COMMENT ON COLUMN tr_svc_quality_prz.praise_time IS '表扬时间';
COMMENT ON COLUMN tr_svc_quality_prz.dept_code IS '科室代码';
COMMENT ON COLUMN tr_svc_quality_prz.dept_name IS '科室';
COMMENT ON COLUMN tr_svc_quality_prz.person_name IS '人员';
COMMENT ON COLUMN tr_svc_quality_prz.position IS '职位';
COMMENT ON COLUMN tr_svc_quality_prz.method IS '表扬方式';
COMMENT ON COLUMN tr_svc_quality_prz.feedback IS '反馈内容';
COMMENT ON COLUMN tr_svc_quality_prz.remark IS '备注';
COMMENT ON COLUMN tr_svc_quality_prz.create_time IS '创建时间';
COMMENT ON COLUMN tr_svc_quality_prz.update_time IS '更新时间';
COMMENT ON COLUMN tr_svc_quality_prz.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_svc_quality_prz.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_svc_quality_prz.ext3 IS '扩展字段3';
COMMENT ON COLUMN tr_common_dict.id IS '主键ID';
COMMENT ON COLUMN tr_common_dict.dict_type IS '字典类型：position/complaintCategory/complaintResult/praiseMethod/feedback';
COMMENT ON COLUMN tr_common_dict.dict_code IS '字典编码';
COMMENT ON COLUMN tr_common_dict.dict_name IS '字典名称';
COMMENT ON COLUMN tr_common_dict.sort_no IS '排序号';
COMMENT ON COLUMN tr_common_dict.status IS '状态：1启用 0停用';
COMMENT ON COLUMN tr_common_dict.create_time IS '创建时间';
COMMENT ON COLUMN tr_common_dict.update_time IS '更新时间';
COMMENT ON COLUMN tr_common_dict.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_common_dict.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_common_dict.ext3 IS '扩展字段3';
COMMENT ON COLUMN tr_staff_dict.id IS '主键ID';
COMMENT ON COLUMN tr_staff_dict.staff_code IS '人员工号';
COMMENT ON COLUMN tr_staff_dict.staff_name IS '人员姓名';
COMMENT ON COLUMN tr_staff_dict.dept_code IS '所属科室代码';
COMMENT ON COLUMN tr_staff_dict.dept_name IS '所属科室名称';
COMMENT ON COLUMN tr_staff_dict.position IS '岗位类别';
COMMENT ON COLUMN tr_staff_dict.status IS '状态：1在职 0停用';
COMMENT ON COLUMN tr_staff_dict.create_time IS '创建时间';
COMMENT ON COLUMN tr_staff_dict.update_time IS '更新时间';
COMMENT ON COLUMN tr_staff_dict.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_staff_dict.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_staff_dict.ext3 IS '扩展字段3';
