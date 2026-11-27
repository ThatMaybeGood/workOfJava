-- ============================================================
-- 门诊运行数据统计（当前最终结构, 2026-11-27 收敛）
-- 历史已废弃: tr_outp_op_dtl科室明细表、treat_count列, 不再保留
-- ============================================================

-- 1. 门诊运行源表(HIS每日抽取, 报表直接查询)
CREATE TABLE TR_OUTP_OP (
    stat_date               DATE            NOT NULL,   -- 统计日期
    dept_code               VARCHAR2(50),               -- 科室编码(关联TR_DEPT_DICT)
    dept_name               VARCHAR2(100),              -- 科室名称
    total_visits            NUMBER(10)      DEFAULT 0,  -- 总就诊人次(门诊量=本列-return_visits)
    famous_expert           NUMBER(10)      DEFAULT 0,  -- 名医出诊人次
    special_expert          NUMBER(10)      DEFAULT 0,  -- 特需专家出诊人次
    known_expert            NUMBER(10)      DEFAULT 0,  -- 知名专家出诊人次
    expert_a                NUMBER(10)      DEFAULT 0,  -- 专家A出诊人次
    expert_b                NUMBER(10)      DEFAULT 0,  -- 专家B出诊人次
    ordinary                NUMBER(10)      DEFAULT 0,  -- 普通门诊出诊人次
    unit_famous_effective   NUMBER(10)      DEFAULT 0,  -- 名医有效出诊单元
    unit_famous_total       NUMBER(10)      DEFAULT 0,  -- 名医出诊单元总数
    unit_special_effective  NUMBER(10)      DEFAULT 0,  -- 特需有效出诊单元
    unit_special_total      NUMBER(10)      DEFAULT 0,  -- 特需出诊单元总数
    unit_known_effective    NUMBER(10)      DEFAULT 0,  -- 知名专家有效出诊单元
    unit_known_total        NUMBER(10)      DEFAULT 0,  -- 知名专家出诊单元总数
    unit_a_effective        NUMBER(10)      DEFAULT 0,  -- 专家A有效出诊单元
    unit_a_total            NUMBER(10)      DEFAULT 0,  -- 专家A出诊单元总数
    unit_b_effective        NUMBER(10)      DEFAULT 0,  -- 专家B有效出诊单元
    unit_b_total            NUMBER(10)      DEFAULT 0,  -- 专家B出诊单元总数
    unit_ordinary_effective NUMBER(10)      DEFAULT 0,  -- 普通有效出诊单元
    unit_ordinary_total     NUMBER(10)      DEFAULT 0,  -- 普通出诊单元总数
    appointment_total       NUMBER(10)      DEFAULT 0,  -- 预约总数
    appointment_count       NUMBER(10)      DEFAULT 0,  -- 预约数
    return_visits           NUMBER(10)      DEFAULT 0,  -- 退号数
    no_window_count         NUMBER(10)      DEFAULT 0,  -- 非窗口挂号数(预约诊疗率=(本列-return_visits)/(total_visits-return_visits))
    self_pay_total          NUMBER(10)      DEFAULT 0,  -- 自助缴费总数(智慧结算率分母)
    self_settle_count       NUMBER(10)      DEFAULT 0,  -- 自助结算数(智慧结算率分子)
    unit                    VARCHAR2(20),               -- 出诊单元
    create_time             DATE            DEFAULT SYSDATE, -- 创建时间
    update_time             DATE            DEFAULT SYSDATE  -- 更新时间
);
CREATE INDEX idx_tr_outp_op_date ON TR_OUTP_OP(stat_date);
CREATE INDEX idx_tr_outp_op_dept ON TR_OUTP_OP(dept_code);

COMMENT ON TABLE TR_OUTP_OP IS '门诊运行源表(HIS每日抽取,门诊运行数据统计直接查询)';

-- 2. 门诊候诊概览源表(抽数侧建, 每日一条; 概览卡平均等候时长=AVG(avg_waiting_time_new))
CREATE TABLE tr_hrap_opd_overview (
    stat_date            DATE           NOT NULL,       -- 统计日期
    avg_waiting_time_new NUMBER(10,2)   DEFAULT 0       -- 平均等候时长
);

COMMENT ON TABLE tr_hrap_opd_overview IS '门诊候诊概览源表(每日一条, 概览卡平均等候时长取数)';

-- 3. 门诊候诊时长源表(抽数侧建; 科室表格平均等候时长按stat_date+dept_code左连取waiting_time, 空取0)
CREATE TABLE tr_hrap_opd_waiting_time (
    stat_date    DATE           NOT NULL,               -- 统计日期
    dept_code    VARCHAR2(50),                          -- 科室编码
    waiting_time NUMBER(10,2)   DEFAULT 0               -- 等候时长
);

COMMENT ON TABLE tr_hrap_opd_waiting_time IS '门诊候诊时长源表(按科室, 表格平均等候时长取数)';
