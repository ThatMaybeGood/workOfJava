-- ============================================================
-- 门诊运行数据统计
-- ============================================================

-- 清理已存在对象(如重建请先执行)

DROP TABLE tr_outp_op_dtl CASCADE CONSTRAINTS;

-- 1.2 门诊运行科室明细表
CREATE TABLE tr_outp_op_dtl (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    dept_code       VARCHAR2(50),                       -- 科室编码(关联TR_DEPT_DICT)
    dept_name       VARCHAR2(100)   NOT NULL,           -- 科室名称
    visits          NUMBER(10)      DEFAULT 0,          -- 就诊人次
    appointment_rate VARCHAR2(20),                      -- 预约率
    exam_rate       VARCHAR2(20),                      -- 检查率
    efficiency      NUMBER(10,2),                       -- 效率
    visit_count     NUMBER(10)      DEFAULT 0,          -- 就诊人次统计
    famous_expert   NUMBER(10)      DEFAULT 0,          -- 名医
    special_expert  NUMBER(10)      DEFAULT 0,          -- 特需专家
    known_expert    NUMBER(10)      DEFAULT 0,          -- 知名专家
    expert_a        NUMBER(10)      DEFAULT 0,          -- 专家A
    expert_b        NUMBER(10)      DEFAULT 0,          -- 专家B
    ordinary        NUMBER(10)      DEFAULT 0,          -- 普通
    effective_total NUMBER(10)      DEFAULT 0,          -- 有效单元总数
    effective_detail NUMBER(10)    DEFAULT 0,          -- 有效单元明细
    total_detail    NUMBER(10)      DEFAULT 0,          -- 总单元明细
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 1.3 门诊运行源表(HIS每日抽取,报表直接查询)
CREATE TABLE TR_OUTP_OP (
    id                      NUMBER(19)      PRIMARY KEY,
    stat_date               DATE            NOT NULL,   -- 统计日期
    dept_code               VARCHAR2(50),               -- 科室编码
    total_visits            NUMBER(10)      DEFAULT 0,  -- 总就诊人次
    famous_expert           NUMBER(10)      DEFAULT 0,  -- 名医
    special_expert          NUMBER(10)      DEFAULT 0,  -- 特需专家
    known_expert            NUMBER(10)      DEFAULT 0,  -- 知名专家
    expert_a                NUMBER(10)      DEFAULT 0,  -- 专家A
    expert_b                NUMBER(10)      DEFAULT 0,  -- 专家B
    ordinary                NUMBER(10)      DEFAULT 0,  -- 普通
    unit_famous_effective   NUMBER(10)      DEFAULT 0,  -- 名医有效单元
    unit_famous_total       NUMBER(10)      DEFAULT 0,  -- 名医总单元
    unit_special_effective  NUMBER(10)      DEFAULT 0,  -- 特需有效单元
    unit_special_total      NUMBER(10)      DEFAULT 0,  -- 特需总单元
    unit_known_effective    NUMBER(10)      DEFAULT 0,  -- 知名专家有效单元
    unit_known_total        NUMBER(10)      DEFAULT 0,  -- 知名专家总单元
    unit_a_effective        NUMBER(10)      DEFAULT 0,  -- 专家A有效单元
    unit_a_total            NUMBER(10)      DEFAULT 0,  -- 专家A总单元
    unit_b_effective        NUMBER(10)      DEFAULT 0,  -- 专家B有效单元
    unit_b_total            NUMBER(10)      DEFAULT 0,  -- 专家B总单元
    unit_ordinary_effective NUMBER(10)      DEFAULT 0,  -- 普通有效单元
    unit_ordinary_total     NUMBER(10)      DEFAULT 0,  -- 普通总单元
    appointment_total       NUMBER(10)      DEFAULT 0,  -- 预约总数
    appointment_count       NUMBER(10)      DEFAULT 0,  -- 预约数
    return_visits           NUMBER(10)      DEFAULT 0,  -- 复诊人次
    treat_count             NUMBER(10)      DEFAULT 0,  -- 治疗人次
    unit                    VARCHAR2(20)                -- 单元
);
CREATE INDEX idx_tr_outp_op_date ON TR_OUTP_OP(stat_date);
CREATE INDEX idx_tr_outp_op_dept ON TR_OUTP_OP(dept_code);

COMMENT ON TABLE TR_OUTP_OP IS '门诊运行源表(HIS每日抽取,门诊运行数据统计直接查询)';

-- 创建索引

CREATE INDEX idx_tr_op_detail_date ON tr_outp_op_dtl(stat_date);
CREATE INDEX idx_tr_op_detail_dept ON tr_outp_op_dtl(dept_name);

-- 添加注释

COMMENT ON TABLE tr_outp_op_dtl IS '门诊运行数据统计-科室明细';
COMMENT ON TABLE tr_outp_op_dtl IS '门诊运行数据统计-科室明细(按科室维度存储就诊人次、预约率、检查率、效率及按职称分类的就诊人次明细)';
COMMENT ON COLUMN tr_outp_op_dtl.id IS '主键ID';
COMMENT ON COLUMN tr_outp_op_dtl.stat_date IS '统计日期';
COMMENT ON COLUMN tr_outp_op_dtl.dept_name IS '科室名称';
COMMENT ON COLUMN tr_outp_op_dtl.visits IS '就诊人次';
COMMENT ON COLUMN tr_outp_op_dtl.appointment_rate IS '预约率';
COMMENT ON COLUMN tr_outp_op_dtl.exam_rate IS '检查率';
COMMENT ON COLUMN tr_outp_op_dtl.efficiency IS '效率';
COMMENT ON COLUMN tr_outp_op_dtl.visit_count IS '就诊人次统计';
COMMENT ON COLUMN tr_outp_op_dtl.famous_expert IS '名医就诊人次';
COMMENT ON COLUMN tr_outp_op_dtl.special_expert IS '特需专家就诊人次';
COMMENT ON COLUMN tr_outp_op_dtl.known_expert IS '知名专家就诊人次';
COMMENT ON COLUMN tr_outp_op_dtl.expert_a IS '专家A就诊人次';
COMMENT ON COLUMN tr_outp_op_dtl.expert_b IS '专家B就诊人次';
COMMENT ON COLUMN tr_outp_op_dtl.ordinary IS '普通就诊人次';
COMMENT ON COLUMN tr_outp_op_dtl.effective_total IS '有效单元总数';
COMMENT ON COLUMN tr_outp_op_dtl.effective_detail IS '有效单元明细';
COMMENT ON COLUMN tr_outp_op_dtl.total_detail IS '总单元明细';
COMMENT ON COLUMN tr_outp_op_dtl.create_time IS '创建时间';
COMMENT ON COLUMN tr_outp_op_dtl.update_time IS '更新时间';
COMMENT ON COLUMN tr_outp_op_dtl.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_outp_op_dtl.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_outp_op_dtl.ext3 IS '扩展字段3';
