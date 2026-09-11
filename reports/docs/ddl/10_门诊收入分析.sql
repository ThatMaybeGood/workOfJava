-- ============================================================
-- 门诊收入分析
-- ============================================================

-- 清理已存在对象(如重建请先执行)

DROP TABLE tr_rev_ov CASCADE CONSTRAINTS;

-- 10.1 门诊收入概览表
CREATE TABLE tr_rev_ov (
    id                  NUMBER(19)      PRIMARY KEY,
    stat_date           DATE            NOT NULL,       -- 统计日期
    dept_code           VARCHAR2(50),                   -- 科室编码
    register_revenue    NUMBER(18,2),                   -- 挂号收入
    medical_revenue     NUMBER(18,2),                   -- 医疗收入
    outpatient_revenue  NUMBER(18,2),                   -- 门诊收入
    service_revenue     NUMBER(18,2),                   -- 服务收入
    create_time         DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time         DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1                VARCHAR2(500),                        -- 扩展字段1
    ext2                VARCHAR2(500),                        -- 扩展字段2
    ext3                VARCHAR2(500)                         -- 扩展字段3
);

-- 创建索引

CREATE INDEX idx_tr_rev_ov_date ON tr_rev_ov(stat_date);

-- 添加注释

COMMENT ON TABLE tr_rev_ov IS '门诊收入分析-概览';
COMMENT ON TABLE tr_rev_ov IS '门诊收入分析-概览(门诊收入的总览数据:门诊总收入和服务收入)';
COMMENT ON COLUMN tr_rev_ov.id IS '主键ID';
COMMENT ON COLUMN tr_rev_ov.stat_date IS '统计日期';
COMMENT ON COLUMN tr_rev_ov.dept_code IS '科室编码';
COMMENT ON COLUMN tr_rev_ov.register_revenue IS '挂号收入';
COMMENT ON COLUMN tr_rev_ov.medical_revenue IS '医疗收入';
COMMENT ON COLUMN tr_rev_ov.outpatient_revenue IS '门诊收入';
COMMENT ON COLUMN tr_rev_ov.service_revenue IS '服务收入';
COMMENT ON COLUMN tr_rev_ov.create_time IS '创建时间';
COMMENT ON COLUMN tr_rev_ov.update_time IS '更新时间';
COMMENT ON COLUMN tr_rev_ov.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_rev_ov.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_rev_ov.ext3 IS '扩展字段3';
