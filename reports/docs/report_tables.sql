-- ============================================================
-- 报表平台数据库表结构 - Oracle
-- 表名前缀:tr_ (table report)
-- 每个表预留3个扩展字段:ext1, ext2, ext3
-- 生成日期:2026-06-16
-- ============================================================

-- ============================================================
-- 清理已存在对象(如重建请先执行)
-- ============================================================

DROP SEQUENCE seq_tr_reports;

DROP TABLE tr_outp_op_ov CASCADE CONSTRAINTS;
DROP TABLE tr_outp_op_dtl CASCADE CONSTRAINTS;
DROP TABLE tr_outp_alt_ov CASCADE CONSTRAINTS;
DROP TABLE tr_outp_alt_dept CASCADE CONSTRAINTS;
DROP TABLE tr_outp_alt_doc CASCADE CONSTRAINTS;
DROP TABLE tr_outp_fc_ov CASCADE CONSTRAINTS;
DROP TABLE tr_outp_fc_month CASCADE CONSTRAINTS;
DROP TABLE tr_outp_fc_year CASCADE CONSTRAINTS;
DROP TABLE tr_inet_hosp_ov CASCADE CONSTRAINTS;
DROP TABLE tr_inet_hosp_op CASCADE CONSTRAINTS;
DROP TABLE tr_inet_hosp_biz CASCADE CONSTRAINTS;
DROP TABLE tr_inet_hosp_dept_rnk CASCADE CONSTRAINTS;
DROP TABLE tr_inet_hosp_doc_rnk CASCADE CONSTRAINTS;
DROP TABLE tr_inet_hosp_grw CASCADE CONSTRAINTS;
DROP TABLE tr_labstat_ov CASCADE CONSTRAINTS;
DROP TABLE tr_labstat_rnk CASCADE CONSTRAINTS;
DROP TABLE tr_labstat_tm CASCADE CONSTRAINTS;
DROP TABLE tr_medtech_ov CASCADE CONSTRAINTS;
DROP TABLE tr_medtech_dtl CASCADE CONSTRAINTS;
DROP TABLE tr_noshow_ov CASCADE CONSTRAINTS;
DROP TABLE tr_noshow_dtl CASCADE CONSTRAINTS;
DROP TABLE tr_noshow_org CASCADE CONSTRAINTS;
DROP TABLE tr_noshow_chn CASCADE CONSTRAINTS;
DROP TABLE tr_noshow_age CASCADE CONSTRAINTS;
DROP TABLE tr_pat_portrait_age CASCADE CONSTRAINTS;
DROP TABLE tr_pat_portrait_insur CASCADE CONSTRAINTS;
DROP TABLE tr_pat_portrait_idty CASCADE CONSTRAINTS;
DROP TABLE tr_pat_portrait_reg CASCADE CONSTRAINTS;
DROP TABLE tr_pat_portrait_arc CASCADE CONSTRAINTS;
DROP TABLE tr_qc_ov CASCADE CONSTRAINTS;
DROP TABLE tr_qc_dtl CASCADE CONSTRAINTS;
DROP TABLE tr_rev_ov CASCADE CONSTRAINTS;
DROP TABLE tr_rev_dept CASCADE CONSTRAINTS;
DROP TABLE tr_rev_doc CASCADE CONSTRAINTS;
DROP TABLE tr_room_use_ov CASCADE CONSTRAINTS;
DROP TABLE tr_room_use_dtl CASCADE CONSTRAINTS;
DROP TABLE tr_svc_quality_ov CASCADE CONSTRAINTS;
DROP TABLE tr_svc_quality_cmpl CASCADE CONSTRAINTS;
DROP TABLE tr_svc_quality_prz CASCADE CONSTRAINTS;
DROP TABLE tr_spec_treat_ov CASCADE CONSTRAINTS;
DROP TABLE tr_spec_treat_dtl CASCADE CONSTRAINTS;
DROP TABLE tr_win_stat_ov CASCADE CONSTRAINTS;
DROP TABLE tr_win_stat_age CASCADE CONSTRAINTS;
DROP TABLE tr_win_stat_tm CASCADE CONSTRAINTS;
DROP TABLE tr_win_stat_src CASCADE CONSTRAINTS;
DROP TABLE tr_win_stat_load CASCADE CONSTRAINTS;
DROP TABLE tr_cash_settle_ov CASCADE CONSTRAINTS;
DROP TABLE tr_cash_settle_dtl CASCADE CONSTRAINTS;
DROP TABLE tr_cash_settle_cht CASCADE CONSTRAINTS;
DROP TABLE tr_disch_settle_ov CASCADE CONSTRAINTS;
DROP TABLE tr_disch_settle_dtl CASCADE CONSTRAINTS;
DROP TABLE tr_disch_settle_cht CASCADE CONSTRAINTS;
DROP TABLE tr_treat_stat_ov CASCADE CONSTRAINTS;
DROP TABLE tr_treat_stat_dtl CASCADE CONSTRAINTS;
DROP TABLE tr_treat_stat_trend CASCADE CONSTRAINTS;
DROP TABLE tr_inpat_prepay_ov CASCADE CONSTRAINTS;
DROP TABLE tr_inpat_prepay_dtl CASCADE CONSTRAINTS;
DROP TABLE tr_inpat_prepay_cht CASCADE CONSTRAINTS;

-- ============================================================
-- 1. 门诊运行数据统计
-- ============================================================

-- 1.1 门诊运行概览表
CREATE TABLE tr_outp_op_ov (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    total_visits    NUMBER(10)      DEFAULT 0,          -- 总就诊人次
    appointment_rate VARCHAR2(20),                      -- 预约率
    visit_count     NUMBER(10)      DEFAULT 0,          -- 就诊人次
    exam_rate       VARCHAR2(20),                      -- 检查率
    efficiency      NUMBER(10,2),                       -- 效率
    effective_units NUMBER(10)      DEFAULT 0,          -- 有效单元数
    total_units     NUMBER(10)      DEFAULT 0,          -- 总单元数
    -- 就诊人次明细
    famous_expert   NUMBER(10)      DEFAULT 0,          -- 名医
    special_expert  NUMBER(10)      DEFAULT 0,          -- 特需专家
    known_expert    NUMBER(10)      DEFAULT 0,          -- 知名专家
    expert_a        NUMBER(10)      DEFAULT 0,          -- 专家A
    expert_b        NUMBER(10)      DEFAULT 0,          -- 专家B
    ordinary        NUMBER(10)      DEFAULT 0,          -- 普通
    -- 单元明细
    unit_famous_effective NUMBER(10) DEFAULT 0,          -- 名医有效单元
    unit_famous_total     NUMBER(10) DEFAULT 0,          -- 名医总单元
    unit_special_effective NUMBER(10) DEFAULT 0,          -- 特需有效单元
    unit_special_total     NUMBER(10) DEFAULT 0,          -- 特需总单元
    unit_known_effective   NUMBER(10) DEFAULT 0,          -- 知名专家有效单元
    unit_known_total       NUMBER(10) DEFAULT 0,          -- 知名专家总单元
    unit_a_effective       NUMBER(10) DEFAULT 0,          -- 专家A有效单元
    unit_a_total           NUMBER(10) DEFAULT 0,          -- 专家A总单元
    unit_b_effective       NUMBER(10) DEFAULT 0,          -- 专家B有效单元
    unit_b_total           NUMBER(10) DEFAULT 0,          -- 专家B总单元
    unit_ordinary_effective  NUMBER(10) DEFAULT 0,          -- 普通有效单元
    unit_ordinary_total      NUMBER(10) DEFAULT 0,          -- 普通总单元
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 1.2 门诊运行科室明细表
CREATE TABLE tr_outp_op_dtl (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
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

-- ============================================================
-- 2. 门诊预警统计
-- ============================================================

-- 2.1 门诊预警概览表
CREATE TABLE tr_outp_alt_ov (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    remain_alert    NUMBER(10)      DEFAULT 0,          -- 滞留预警
    appointment_alert NUMBER(10)    DEFAULT 0,          -- 预约预警
    early_leave     NUMBER(10)      DEFAULT 0,          -- 早退人数
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 2.2 门诊预警科室明细表
CREATE TABLE tr_outp_alt_dept (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    dept_name       VARCHAR2(100)   NOT NULL,           -- 科室名称
    remain_alert    NUMBER(10)      DEFAULT 0,          -- 滞留预警
    appointment_alert NUMBER(10)    DEFAULT 0,          -- 预约预警
    early_leave     NUMBER(10)      DEFAULT 0,          -- 早退人数
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 2.3 门诊预警医生明细表
CREATE TABLE tr_outp_alt_doc (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    doctor_name     VARCHAR2(100)   NOT NULL,           -- 医生姓名
    dept_name       VARCHAR2(100),                      -- 科室名称
    remain_alert    NUMBER(10)      DEFAULT 0,          -- 滞留预警
    appointment_alert NUMBER(10)    DEFAULT 0,          -- 预约预警
    early_leave     NUMBER(10)      DEFAULT 0,          -- 早退人数
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- ============================================================
-- 3. 预测门诊量报表
-- ============================================================

-- 3.1 预测门诊量概览表
CREATE TABLE tr_outp_fc_ov (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    tomorrow        NUMBER(10)      DEFAULT 0,          -- 明日预测
    next_week       NUMBER(10)      DEFAULT 0,          -- 下周预测
    next_month      NUMBER(10)      DEFAULT 0,          -- 下月预测
    next_year       NUMBER(10)      DEFAULT 0,          -- 明年预测
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 3.2 预测门诊量30天明细表
CREATE TABLE tr_outp_fc_month (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    forecast_date   DATE            NOT NULL,           -- 预测日期
    forecast_value  NUMBER(10)      DEFAULT 0,          -- 预测值
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 3.3 预测门诊量12个月明细表
CREATE TABLE tr_outp_fc_year (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    forecast_month  VARCHAR2(20)    NOT NULL,           -- 预测月份(YYYY-MM)
    forecast_value  NUMBER(10)      DEFAULT 0,          -- 预测值
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- ============================================================
-- 4. 互医质控运营月报
-- ============================================================

-- 4.1 互医质控概览表
CREATE TABLE tr_inet_hosp_ov (
    id              NUMBER(19)      PRIMARY KEY,
    stat_month      VARCHAR2(20)    NOT NULL,           -- 统计月份(YYYY-MM)
    outpatient_volume NUMBER(10)    DEFAULT 0,          -- 门诊量
    doctor_ratio    VARCHAR2(20),                      -- 医师占比
    reception_rate  VARCHAR2(20),                      -- 接诊率
    prescription_rate VARCHAR2(20),                    -- 处方率
    record_rate     VARCHAR2(20),                      -- 病历率
    review_rate     VARCHAR2(20),                      -- 审方率
    execution_rate  VARCHAR2(20),                      -- 执行率
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 4.2 互医质控运行情况表
CREATE TABLE tr_inet_hosp_op (
    id              NUMBER(19)      PRIMARY KEY,
    stat_month      VARCHAR2(20)    NOT NULL,           -- 统计月份(YYYY-MM)
    item_name       VARCHAR2(100)   NOT NULL,           -- 指标名称
    current_value   NUMBER(10)      DEFAULT 0,          -- 当月值
    last_value      NUMBER(10)      DEFAULT 0,          -- 上月值
    growth_rate     VARCHAR2(20),                      -- 增长率
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 4.3 互医质控业务分析图表表
CREATE TABLE tr_inet_hosp_biz (
    id              NUMBER(19)      PRIMARY KEY,
    stat_month      VARCHAR2(20)    NOT NULL,           -- 统计月份(YYYY-MM)
    category        VARCHAR2(100)   NOT NULL,           -- 分类
    current_value   NUMBER(10)      DEFAULT 0,          -- 当月值
    last_value      NUMBER(10)      DEFAULT 0,          -- 上月值
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 4.4 互医质控科室排行表
CREATE TABLE tr_inet_hosp_dept_rnk (
    id              NUMBER(19)      PRIMARY KEY,
    stat_month      VARCHAR2(20)    NOT NULL,           -- 统计月份(YYYY-MM)
    rank_num        NUMBER(5)       DEFAULT 0,          -- 排名
    dept_name       VARCHAR2(100)   NOT NULL,           -- 科室名称
    current_month   NUMBER(10)      DEFAULT 0,          -- 当月值
    last_month      NUMBER(10)      DEFAULT 0,          -- 上月值
    growth_rate     VARCHAR2(20),                      -- 增长率
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 4.5 互医质控医生排行表
CREATE TABLE tr_inet_hosp_doc_rnk (
    id              NUMBER(19)      PRIMARY KEY,
    stat_month      VARCHAR2(20)    NOT NULL,           -- 统计月份(YYYY-MM)
    rank_num        NUMBER(5)       DEFAULT 0,          -- 排名
    doctor_name     VARCHAR2(100)   NOT NULL,           -- 医生姓名
    dept_name       VARCHAR2(100),                      -- 科室名称
    title           VARCHAR2(50),                       -- 职称
    current_month   NUMBER(10)      DEFAULT 0,          -- 当月值
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 4.6 互医质控增长趋势表
CREATE TABLE tr_inet_hosp_grw (
    id              NUMBER(19)      PRIMARY KEY,
    stat_month      VARCHAR2(20)    NOT NULL,           -- 统计月份(YYYY-MM)
    category        VARCHAR2(100)   NOT NULL,           -- 分类(月份)
    data_value      NUMBER(10)      DEFAULT 0,          -- 数值
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- ============================================================
-- 5. 检验统计
-- ============================================================

-- 5.1 检验统计概览表
CREATE TABLE tr_labstat_ov (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    blood_collection NUMBER(10)     DEFAULT 0,          -- 采血人次
    blood_efficiency VARCHAR2(20),                     -- 采血效率
    lab_efficiency  VARCHAR2(20),                      -- 检验效率
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 5.2 检验统计排行表
CREATE TABLE tr_labstat_rnk (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    rank_type       VARCHAR2(50)    NOT NULL,           -- 排行类型(BLOOD/LAB)
    rank_num        NUMBER(5)       DEFAULT 0,          -- 排名
    item_name       VARCHAR2(100)   NOT NULL,           -- 项目名称
    item_value      NUMBER(10)      DEFAULT 0,          -- 项目值
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 5.3 检验统计时段分析表
CREATE TABLE tr_labstat_tm (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    time_slot       VARCHAR2(50)    NOT NULL,           -- 时段
    blood_count     NUMBER(10)      DEFAULT 0,          -- 采血人次
    lab_count       NUMBER(10)      DEFAULT 0,          -- 检验人次
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- ============================================================
-- 6. 医技统计
-- ============================================================

-- 6.1 医技统计概览表
CREATE TABLE tr_medtech_ov (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    check_count     NUMBER(10)      DEFAULT 0,          -- 检查人次
    on_time_rate    VARCHAR2(20),                      -- 准时率
    wait_time       VARCHAR2(20),                      -- 等候时长
    avg_wait_late   VARCHAR2(20),                      -- 平均迟到
    avg_report_time VARCHAR2(20),                      -- 平均报告时长
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 6.2 医技统计科室明细表
CREATE TABLE tr_medtech_dtl (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    dept_name       VARCHAR2(100)   NOT NULL,           -- 科室名称
    check_count     NUMBER(10)      DEFAULT 0,          -- 检查人次
    on_time_rate    VARCHAR2(20),                      -- 准时率
    wait_time       NUMBER(10,2),                       -- 等候时长
    avg_wait_late   NUMBER(10,2),                       -- 平均迟到
    avg_report_time NUMBER(10,2),                       -- 平均报告时长
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- ============================================================
-- 7. 爽约退号分析
-- ============================================================

-- 7.1 爽约退号概览表
CREATE TABLE tr_noshow_ov (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    refund_count    NUMBER(10)      DEFAULT 0,          -- 退号人数
    refund_rate     VARCHAR2(20),                      -- 退号率
    no_show_count   NUMBER(10)      DEFAULT 0,          -- 爽约人数
    no_show_rate    VARCHAR2(20),                      -- 爽约率
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 7.2 爽约退号科室明细表
CREATE TABLE tr_noshow_dtl (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    dept_name       VARCHAR2(100)   NOT NULL,           -- 科室名称
    refund_count    NUMBER(10)      DEFAULT 0,          -- 退号人数
    refund_rate     VARCHAR2(20),                      -- 退号率
    no_show_count   NUMBER(10)      DEFAULT 0,          -- 爽约人数
    no_show_rate    VARCHAR2(20),                      -- 爽约率
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 7.3 爽约退号来源分析表
CREATE TABLE tr_noshow_org (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    origin_type     VARCHAR2(50)    NOT NULL,           -- 来源类型(REFUND/NO_SHOW)
    item_name       VARCHAR2(100)   NOT NULL,           -- 来源名称
    item_value      NUMBER(10)      DEFAULT 0,          -- 数量
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 7.4 爽约退号渠道分析表
CREATE TABLE tr_noshow_chn (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    channel_type    VARCHAR2(50)    NOT NULL,           -- 渠道类型(REFUND/NO_SHOW)
    item_name       VARCHAR2(100)   NOT NULL,           -- 渠道名称
    item_value      NUMBER(10)      DEFAULT 0,          -- 数量
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 7.5 爽约退号年龄分析表
CREATE TABLE tr_noshow_age (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    age_group       VARCHAR2(50)    NOT NULL,           -- 年龄段
    no_show_count   NUMBER(10)      DEFAULT 0,          -- 爽约人数
    refund_count    NUMBER(10)      DEFAULT 0,          -- 退号人数
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- ============================================================
-- 8. 患者画像
-- ============================================================

-- 8.1 患者画像年龄分析表
CREATE TABLE tr_pat_portrait_age (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
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
    insurance_type  VARCHAR2(100)   NOT NULL,           -- 医保类型
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
    identity_type   VARCHAR2(100)   NOT NULL,           -- 身份类型
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
    source_type     VARCHAR2(100)   NOT NULL,           -- 来源类型
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
    source_type     VARCHAR2(100)   NOT NULL,           -- 来源类型
    patient_count   NUMBER(10)      DEFAULT 0,          -- 患者人数
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- ============================================================
-- 9. 门诊管理质量控制
-- ============================================================

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

-- ============================================================
-- 10. 门诊收入分析
-- ============================================================

-- 10.1 门诊收入概览表
CREATE TABLE tr_rev_ov (
    id                  NUMBER(19)      PRIMARY KEY,
    stat_date           DATE            NOT NULL,       -- 统计日期
    outpatient_revenue  NUMBER(18,2),                   -- 门诊收入
    service_revenue     NUMBER(18,2),                   -- 服务收入
    create_time         DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time         DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1                VARCHAR2(500),                        -- 扩展字段1
    ext2                VARCHAR2(500),                        -- 扩展字段2
    ext3                VARCHAR2(500)                         -- 扩展字段3
);

-- 10.2 门诊收入科室明细表
CREATE TABLE tr_rev_dept (
    id                  NUMBER(19)      PRIMARY KEY,
    stat_date           DATE            NOT NULL,       -- 统计日期
    dept_name           VARCHAR2(100)   NOT NULL,       -- 科室名称
    outpatient_revenue  VARCHAR2(100),                  -- 门诊收入
    service_revenue     VARCHAR2(100),                  -- 服务收入
    create_time         DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time         DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1                VARCHAR2(500),                        -- 扩展字段1
    ext2                VARCHAR2(500),                        -- 扩展字段2
    ext3                VARCHAR2(500)                         -- 扩展字段3
);

-- 10.3 门诊收入医生明细表
CREATE TABLE tr_rev_doc (
    id                  NUMBER(19)      PRIMARY KEY,
    stat_date           DATE            NOT NULL,       -- 统计日期
    doctor_name         VARCHAR2(100)   NOT NULL,       -- 医生姓名
    dept_name           VARCHAR2(100),                  -- 科室名称
    doctor_benefit      VARCHAR2(100),                  -- 医生收益
    service_revenue     VARCHAR2(100),                  -- 服务收入
    create_time         DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time         DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1                VARCHAR2(500),                        -- 扩展字段1
    ext2                VARCHAR2(500),                        -- 扩展字段2
    ext3                VARCHAR2(500)                         -- 扩展字段3
);

-- ============================================================
-- 11. 诊室使用率分析
-- ============================================================

-- 11.1 诊室使用率概览表
CREATE TABLE tr_room_use_ov (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    avg_usage       VARCHAR2(20),                      -- 平均使用率
    am_usage        VARCHAR2(20),                      -- 上午使用率
    pm_usage        VARCHAR2(20),                      -- 下午使用率
    holiday_usage   VARCHAR2(20),                      -- 节假日使用率
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 11.2 诊室使用率科室明细表
CREATE TABLE tr_room_use_dtl (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    dept_name       VARCHAR2(100)   NOT NULL,           -- 科室名称
    avg_usage       VARCHAR2(20),                      -- 平均使用率
    am_usage        VARCHAR2(20),                      -- 上午使用率
    pm_usage        VARCHAR2(20),                      -- 下午使用率
    holiday_usage   VARCHAR2(20),                      -- 节假日使用率
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- ============================================================
-- 12. 门诊服务质量分析
-- ============================================================

-- 12.1 门诊服务质量概览表
CREATE TABLE tr_svc_quality_ov (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    complaint_count NUMBER(10)      DEFAULT 0,          -- 投诉数量
    praise_count    NUMBER(10)      DEFAULT 0,          -- 表扬数量
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 12.2 门诊服务质量投诉明细表
CREATE TABLE tr_svc_quality_cmpl (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    complaint_time  DATE,                               -- 投诉时间
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

-- ============================================================
-- 13. 专科治疗量统计
-- ============================================================

-- 13.1 专科治疗量概览表
CREATE TABLE tr_spec_treat_ov (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    treatment_count NUMBER(10)      DEFAULT 0,          -- 治疗人次
    treatment_amount NUMBER(18,2),                      -- 治疗金额
    patient_count   NUMBER(10)      DEFAULT 0,          -- 患者人数
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 13.2 专科治疗量科室明细表
CREATE TABLE tr_spec_treat_dtl (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    dept_name       VARCHAR2(100)   NOT NULL,           -- 科室名称
    treatment_count NUMBER(10)      DEFAULT 0,          -- 治疗人次
    treatment_amount NUMBER(18,2),                      -- 治疗金额
    patient_count   NUMBER(10)      DEFAULT 0,          -- 患者人数
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- ============================================================
-- 14. 人工窗口统计
-- ============================================================

-- 14.1 人工窗口概览表
CREATE TABLE tr_win_stat_ov (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    register_count  NUMBER(10)      DEFAULT 0,          -- 挂号人次
    payment_count   NUMBER(10)      DEFAULT 0,          -- 收费人次
    refund_count    NUMBER(10)      DEFAULT 0,          -- 退费人次
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 14.2 人工窗口年龄分析表
CREATE TABLE tr_win_stat_age (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    age_group       VARCHAR2(50)    NOT NULL,           -- 年龄段
    patient_count   NUMBER(10)      DEFAULT 0,          -- 人数
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 14.3 人工窗口时段分析表
CREATE TABLE tr_win_stat_tm (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    time_slot       VARCHAR2(50)    NOT NULL,           -- 时段
    business_count  NUMBER(10)      DEFAULT 0,          -- 业务量
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 14.4 人工窗口来源分析表
CREATE TABLE tr_win_stat_src (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    source_name     VARCHAR2(100)   NOT NULL,           -- 来源名称
    source_count    NUMBER(10)      DEFAULT 0,          -- 数量
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 14.5 人工窗口工作量表
CREATE TABLE tr_win_stat_load (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    business_type   VARCHAR2(100)   NOT NULL,           -- 业务类型
    register_count  NUMBER(10)      DEFAULT 0,          -- 挂号数
    payment_count   NUMBER(10)      DEFAULT 0,          -- 收费数
    refund_count    NUMBER(10)      DEFAULT 0,          -- 退费数
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- ============================================================
-- 15. 收费员结账统计
-- ============================================================

-- 15.1 收费员结账概览表
CREATE TABLE tr_cash_settle_ov (
    id                      NUMBER(19)      PRIMARY KEY,
    stat_date               DATE            NOT NULL,   -- 统计日期
    appointment_register    NUMBER(10)      DEFAULT 0,  -- 预约挂号
    appointment_register_compare NUMBER(10) DEFAULT 0, -- 对比值
    appointment_fetch     NUMBER(10)      DEFAULT 0,  -- 预约取号
    appointment_fetch_compare NUMBER(10) DEFAULT 0,  -- 预约取号对比
    today_register        NUMBER(10)      DEFAULT 0,  -- 当日挂号
    today_register_compare NUMBER(10) DEFAULT 0,  -- 当日挂号对比
    refund                NUMBER(10)      DEFAULT 0,  -- 退号
    refund_compare        NUMBER(10)      DEFAULT 0,  -- 退号对比
    outpatient_charge     NUMBER(10)      DEFAULT 0,  -- 门诊收费
    outpatient_charge_compare NUMBER(10) DEFAULT 0,  -- 门诊收费对比
    outpatient_refund     NUMBER(10)      DEFAULT 0,  -- 门诊退费
    outpatient_refund_compare NUMBER(10) DEFAULT 0,  -- 门诊退费对比
    prepayment            NUMBER(10)      DEFAULT 0,  -- 预交金
    prepayment_compare    NUMBER(10)      DEFAULT 0,  -- 预交金对比
    hospital_refund       NUMBER(10)      DEFAULT 0,  -- 住院退费
    hospital_refund_compare NUMBER(10) DEFAULT 0,  -- 住院退费对比
    discharge_settlement  NUMBER(10)      DEFAULT 0,  -- 出院结算
    discharge_settlement_compare NUMBER(10) DEFAULT 0,  -- 出院结算对比
    create_time           DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time           DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1                  VARCHAR2(500),                        -- 扩展字段1
    ext2                  VARCHAR2(500),                        -- 扩展字段2
    ext3                  VARCHAR2(500)                         -- 扩展字段3
);

-- 15.2 收费员结账日明细表
CREATE TABLE tr_cash_settle_dtl (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    item_date       DATE            NOT NULL,           -- 日期
    cashier_name    VARCHAR2(100),                      -- 收费员
    item_type       VARCHAR2(50)    NOT NULL,           -- 项目类型
    item_value      NUMBER(18,2),                       -- 金额
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 15.3 收费员结账图表数据表
CREATE TABLE tr_cash_settle_cht (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    chart_title     VARCHAR2(200),                      -- 图表标题
    chart_subtitle  VARCHAR2(200),                      -- 副标题
    date_range      VARCHAR2(100),                      -- 日期范围
    category        VARCHAR2(100)   NOT NULL,           -- 分类
    data_value      NUMBER(10)      DEFAULT 0,          -- 数值
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- ============================================================
-- 16. 出院结算报表
-- ============================================================

-- 16.1 出院结算概览表
CREATE TABLE tr_disch_settle_ov (
    id                      NUMBER(19)      PRIMARY KEY,
    stat_date               DATE            NOT NULL,   -- 统计日期
    total_discharge_count   NUMBER(10)      DEFAULT 0,  -- 总出院人次
    total_discharge_compare NUMBER(10)      DEFAULT 0,  -- 总出院对比
    discharged_count        NUMBER(10)      DEFAULT 0,  -- 已出院人次
    discharged_compare      NUMBER(10)      DEFAULT 0,  -- 已出院对比
    not_discharged_count    NUMBER(10)      DEFAULT 0,  -- 未出院人次
    not_discharged_compare  NUMBER(10)      DEFAULT 0,  -- 未出院对比
    settlement_amount       NUMBER(18,2),               -- 结算金额
    settlement_amount_compare NUMBER(10)    DEFAULT 0,  -- 结算金额对比
    create_time             DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time             DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1                    VARCHAR2(500),                        -- 扩展字段1
    ext2                    VARCHAR2(500),                        -- 扩展字段2
    ext3                    VARCHAR2(500)                         -- 扩展字段3
);

-- 16.2 出院结算日明细表
CREATE TABLE tr_disch_settle_dtl (
    id                      NUMBER(19)      PRIMARY KEY,
    stat_date               DATE            NOT NULL,   -- 统计日期
    item_date               DATE            NOT NULL,   -- 日期
    total_last              NUMBER(10)      DEFAULT 0,  -- 总出院上期
    total_current           NUMBER(10)      DEFAULT 0,  -- 总出院本期
    total_compare           NUMBER(10)      DEFAULT 0,  -- 总出院对比
    discharged_last         NUMBER(10)      DEFAULT 0,  -- 已出院上期
    discharged_current      NUMBER(10)      DEFAULT 0,  -- 已出院本期
    discharged_compare      NUMBER(10)      DEFAULT 0,  -- 已出院对比
    not_discharged_last     NUMBER(10)      DEFAULT 0,  -- 未出院上期
    not_discharged_current  NUMBER(10)      DEFAULT 0,  -- 未出院本期
    not_discharged_compare  NUMBER(10)      DEFAULT 0,  -- 未出院对比
    amount_last             NUMBER(18,2),               -- 金额上期
    amount_current          NUMBER(18,2),               -- 金额本期
    amount_compare          NUMBER(10)      DEFAULT 0,  -- 金额对比
    create_time             DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time             DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1                    VARCHAR2(500),                        -- 扩展字段1
    ext2                    VARCHAR2(500),                        -- 扩展字段2
    ext3                    VARCHAR2(500)                         -- 扩展字段3
);

-- 16.3 出院结算图表分析表
CREATE TABLE tr_disch_settle_cht (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    chart_type      VARCHAR2(50)    NOT NULL,           -- 图表类型(CHANNEL/PATIENT_TYPE/AMOUNT_TYPE)
    item_name       VARCHAR2(100)   NOT NULL,           -- 项目名称
    item_value      NUMBER(10)      DEFAULT 0,          -- 数值
    item_compare    NUMBER(10)      DEFAULT 0,          -- 对比值
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- ============================================================
-- 创建序列(Oracle 主键自增)
-- ============================================================

CREATE SEQUENCE seq_tr_reports START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 创建索引
-- ============================================================

-- 按统计日期索引
CREATE INDEX idx_tr_op_overview_date ON tr_outp_op_ov(stat_date);
CREATE INDEX idx_tr_op_detail_date ON tr_outp_op_dtl(stat_date);
CREATE INDEX idx_tr_alert_overview_date ON tr_outp_alt_ov(stat_date);
CREATE INDEX idx_tr_alert_dept_date ON tr_outp_alt_dept(stat_date);
CREATE INDEX idx_tr_alert_doctor_date ON tr_outp_alt_doc(stat_date);
CREATE INDEX idx_tr_forecast_overview_date ON tr_outp_fc_ov(stat_date);
CREATE INDEX idx_tr_internet_overview_month ON tr_inet_hosp_ov(stat_month);
CREATE INDEX idx_tr_lab_overview_date ON tr_labstat_ov(stat_date);
CREATE INDEX idx_tr_med_overview_date ON tr_medtech_ov(stat_date);
CREATE INDEX idx_tr_noshow_ov_date ON tr_noshow_ov(stat_date);
CREATE INDEX idx_tr_portrait_age_date ON tr_pat_portrait_age(stat_date);
CREATE INDEX idx_tr_qc_overview_date ON tr_qc_ov(stat_date);
CREATE INDEX idx_tr_rev_ov_date ON tr_rev_ov(stat_date);
CREATE INDEX idx_tr_room_overview_date ON tr_room_use_ov(stat_date);
CREATE INDEX idx_tr_service_overview_date ON tr_svc_quality_ov(stat_date);
CREATE INDEX idx_tr_specialty_overview_date ON tr_spec_treat_ov(stat_date);
CREATE INDEX idx_tr_window_overview_date ON tr_win_stat_ov(stat_date);
CREATE INDEX idx_tr_cashier_overview_date ON tr_cash_settle_ov(stat_date);
CREATE INDEX idx_tr_discharge_overview_date ON tr_disch_settle_ov(stat_date);
CREATE INDEX idx_tr_treat_overview_date ON tr_treat_stat_ov(stat_date);
CREATE INDEX idx_tr_treat_detail_date ON tr_treat_stat_dtl(stat_date);
CREATE INDEX idx_tr_treat_trend_date ON tr_treat_stat_trend(stat_date);
CREATE INDEX idx_tr_prepay_overview_date ON tr_inpat_prepay_ov(stat_date);
CREATE INDEX idx_tr_prepay_detail_date ON tr_inpat_prepay_dtl(stat_date);
CREATE INDEX idx_tr_prepay_chart_date ON tr_inpat_prepay_cht(stat_date);

-- 按科室名称索引
CREATE INDEX idx_tr_op_detail_dept ON tr_outp_op_dtl(dept_name);
CREATE INDEX idx_tr_alert_dept_name ON tr_outp_alt_dept(dept_name);
CREATE INDEX idx_tr_alert_doctor_name ON tr_outp_alt_doc(doctor_name);
CREATE INDEX idx_tr_med_detail_dept ON tr_medtech_dtl(dept_name);
CREATE INDEX idx_tr_noshow_dtl_dept ON tr_noshow_dtl(dept_name);
CREATE INDEX idx_tr_rev_dept_name ON tr_rev_dept(dept_name);
CREATE INDEX idx_tr_rev_doc_name ON tr_rev_doc(doctor_name);
CREATE INDEX idx_tr_room_detail_dept ON tr_room_use_dtl(dept_name);
CREATE INDEX idx_tr_specialty_detail_dept ON tr_spec_treat_dtl(dept_name);

-- ============================================================
-- 添加表注释
-- ============================================================

COMMENT ON TABLE tr_outp_op_ov IS '门诊运行数据统计-概览';
COMMENT ON TABLE tr_outp_op_dtl IS '门诊运行数据统计-科室明细';
COMMENT ON TABLE tr_outp_alt_ov IS '门诊预警统计-概览';
COMMENT ON TABLE tr_outp_alt_dept IS '门诊预警统计-科室明细';
COMMENT ON TABLE tr_outp_alt_doc IS '门诊预警统计-医生明细';
COMMENT ON TABLE tr_outp_fc_ov IS '预测门诊量报表-概览';
COMMENT ON TABLE tr_outp_fc_month IS '预测门诊量报表-30天明细';
COMMENT ON TABLE tr_outp_fc_year IS '预测门诊量报表-12个月明细';
COMMENT ON TABLE tr_inet_hosp_ov IS '互医质控运营月报-概览';
COMMENT ON TABLE tr_inet_hosp_op IS '互医质控运营月报-运行情况';
COMMENT ON TABLE tr_inet_hosp_biz IS '互医质控运营月报-业务分析';
COMMENT ON TABLE tr_inet_hosp_dept_rnk IS '互医质控运营月报-科室排行';
COMMENT ON TABLE tr_inet_hosp_doc_rnk IS '互医质控运营月报-医生排行';
COMMENT ON TABLE tr_inet_hosp_grw IS '互医质控运营月报-增长趋势';
COMMENT ON TABLE tr_labstat_ov IS '检验统计-概览';
COMMENT ON TABLE tr_labstat_rnk IS '检验统计-排行';
COMMENT ON TABLE tr_labstat_tm IS '检验统计-时段分析';
COMMENT ON TABLE tr_medtech_ov IS '医技统计-概览';
COMMENT ON TABLE tr_medtech_dtl IS '医技统计-科室明细';
COMMENT ON TABLE tr_noshow_ov IS '爽约退号分析-概览';
COMMENT ON TABLE tr_noshow_dtl IS '爽约退号分析-科室明细';
COMMENT ON TABLE tr_noshow_org IS '爽约退号分析-来源分析';
COMMENT ON TABLE tr_noshow_chn IS '爽约退号分析-渠道分析';
COMMENT ON TABLE tr_noshow_age IS '爽约退号分析-年龄分析';
COMMENT ON TABLE tr_pat_portrait_age IS '患者画像-年龄分析';
COMMENT ON TABLE tr_pat_portrait_insur IS '患者画像-医保分析';
COMMENT ON TABLE tr_pat_portrait_idty IS '患者画像-身份分析';
COMMENT ON TABLE tr_pat_portrait_reg IS '患者画像-挂号来源';
COMMENT ON TABLE tr_pat_portrait_arc IS '患者画像-建档来源';
COMMENT ON TABLE tr_qc_ov IS '门诊管理质量控制-概览';
COMMENT ON TABLE tr_qc_dtl IS '门诊管理质量控制-月度明细';
COMMENT ON TABLE tr_rev_ov IS '门诊收入分析-概览';
COMMENT ON TABLE tr_rev_dept IS '门诊收入分析-科室明细';
COMMENT ON TABLE tr_rev_doc IS '门诊收入分析-医生明细';
COMMENT ON TABLE tr_room_use_ov IS '诊室使用率分析-概览';
COMMENT ON TABLE tr_room_use_dtl IS '诊室使用率分析-科室明细';
COMMENT ON TABLE tr_svc_quality_ov IS '门诊服务质量分析-概览';
COMMENT ON TABLE tr_svc_quality_cmpl IS '门诊服务质量分析-投诉明细';
COMMENT ON TABLE tr_svc_quality_prz IS '门诊服务质量分析-表扬明细';
COMMENT ON TABLE tr_spec_treat_ov IS '专科治疗量统计-概览';
COMMENT ON TABLE tr_spec_treat_dtl IS '专科治疗量统计-科室明细';
COMMENT ON TABLE tr_win_stat_ov IS '人工窗口统计-概览';
COMMENT ON TABLE tr_win_stat_age IS '人工窗口统计-年龄分析';
COMMENT ON TABLE tr_win_stat_tm IS '人工窗口统计-时段分析';
COMMENT ON TABLE tr_win_stat_src IS '人工窗口统计-来源分析';
COMMENT ON TABLE tr_win_stat_load IS '人工窗口统计-工作量';
COMMENT ON TABLE tr_cash_settle_ov IS '收费员结账统计-概览';
COMMENT ON TABLE tr_cash_settle_dtl IS '收费员结账统计-日明细';
COMMENT ON TABLE tr_cash_settle_cht IS '收费员结账统计-图表';
COMMENT ON TABLE tr_disch_settle_ov IS '出院结算报表-概览';
COMMENT ON TABLE tr_disch_settle_dtl IS '出院结算报表-日明细';
COMMENT ON TABLE tr_disch_settle_cht IS '出院结算报表-图表';

-- ============================================================
-- 添加表注释
-- ============================================================

COMMENT ON TABLE tr_outp_op_ov IS '门诊运行数据统计-概览(存储每日门诊运行总览指标:就诊人次、预约率、检查率、效率、单元数等)';
COMMENT ON TABLE tr_outp_op_dtl IS '门诊运行数据统计-科室明细(按科室维度存储就诊人次、预约率、检查率、效率及按职称分类的就诊人次明细)';
COMMENT ON TABLE tr_outp_alt_ov IS '门诊预警统计-概览(存储每日门诊预警总览:滞留预警、预约预警、早退人数)';
COMMENT ON TABLE tr_outp_alt_dept IS '门诊预警统计-科室明细(按科室维度统计滞留预警、预约预警、早退人数)';
COMMENT ON TABLE tr_outp_alt_doc IS '门诊预警统计-医生明细(按医生维度统计滞留预警、预约预警、早退人数)';
COMMENT ON TABLE tr_outp_fc_ov IS '预测门诊量报表-概览(存储门诊量预测总览:明日、下周、下月、明年的预测值)';
COMMENT ON TABLE tr_outp_fc_month IS '预测门诊量报表-30天明细(存储未来30天每日门诊量预测值)';
COMMENT ON TABLE tr_outp_fc_year IS '预测门诊量报表-12个月明细(存储未来12个月每月门诊量预测值)';
COMMENT ON TABLE tr_inet_hosp_ov IS '互医质控运营月报-概览(存储互联网医院月度质控总览指标:门诊量、接诊率、处方率、审方率等)';
COMMENT ON TABLE tr_inet_hosp_op IS '互医质控运营月报-运行情况(互联网医院各运营指标的运行情况对比:当月 vs 上月)';
COMMENT ON TABLE tr_inet_hosp_biz IS '互医质控运营月报-业务分析(互联网医院业务分析图表数据,按分类存储当月/上月对比)';
COMMENT ON TABLE tr_inet_hosp_dept_rnk IS '互医质控运营月报-科室排行(互联网医院按科室的门诊量排行数据)';
COMMENT ON TABLE tr_inet_hosp_doc_rnk IS '互医质控运营月报-医生排行(互联网医院按医生的门诊量排行数据)';
COMMENT ON TABLE tr_inet_hosp_grw IS '互医质控运营月报-增长趋势(互联网医院各月份的增长趋势数据,用于绘制趋势图)';
COMMENT ON TABLE tr_labstat_ov IS '检验统计-概览(检验科运营总览指标:采血人次、采血效率、检验效率)';
COMMENT ON TABLE tr_labstat_rnk IS '检验统计-排行(检验项目或科室的排行数据:采血排行/检验排行)';
COMMENT ON TABLE tr_labstat_tm IS '检验统计-时段分析(检验科各时段的业务量分布)';
COMMENT ON TABLE tr_medtech_ov IS '医技统计-概览(医技科室运营总览指标:检查人次、准时率、等候时长、报告时长)';
COMMENT ON TABLE tr_medtech_dtl IS '医技统计-科室明细(按医技科室维度统计检查人次、准时率、等候时长等)';
COMMENT ON TABLE tr_noshow_ov IS '爽约退号分析-概览(爽约和退号情况的总览指标)';
COMMENT ON TABLE tr_noshow_dtl IS '爽约退号分析-科室明细(按科室维度统计爽约退号数据)';
COMMENT ON TABLE tr_noshow_org IS '爽约退号分析-来源分析(按挂号来源维度分析爽约/退号分布)';
COMMENT ON TABLE tr_noshow_chn IS '爽约退号分析-渠道分析(按渠道维度分析爽约/退号分布)';
COMMENT ON TABLE tr_noshow_age IS '爽约退号分析-年龄分析(按年龄段分析爽约/退号分布)';
COMMENT ON TABLE tr_pat_portrait_age IS '患者画像-年龄分析(按年龄段分析患者建档和门诊就诊分布)';
COMMENT ON TABLE tr_pat_portrait_insur IS '患者画像-医保分析(按医保类型分析患者分布)';
COMMENT ON TABLE tr_pat_portrait_idty IS '患者画像-身份分析(按患者身份类型分析分布)';
COMMENT ON TABLE tr_pat_portrait_reg IS '患者画像-挂号来源(按挂号来源分析患者分布)';
COMMENT ON TABLE tr_pat_portrait_arc IS '患者画像-建档来源(按建档来源分析患者分布)';
COMMENT ON TABLE tr_qc_ov IS '门诊管理质量控制-概览(门诊质量管理各项质控指标的总览数据)';
COMMENT ON TABLE tr_qc_dtl IS '门诊管理质量控制-月度明细(门诊质控指标按月度的详细记录,用于趋势分析)';
COMMENT ON TABLE tr_rev_ov IS '门诊收入分析-概览(门诊收入的总览数据:门诊总收入和服务收入)';
COMMENT ON TABLE tr_rev_dept IS '门诊收入分析-科室明细(按科室维度统计门诊收入和服务收入)';
COMMENT ON TABLE tr_rev_doc IS '门诊收入分析-医生明细(按医生维度统计门诊收益和服务收入)';
COMMENT ON TABLE tr_room_use_ov IS '诊室使用率分析-概览(诊室使用率的总览指标:平均使用率、上午/下午/节假日使用率)';
COMMENT ON TABLE tr_room_use_dtl IS '诊室使用率分析-科室明细(按科室维度统计诊室使用率)';
COMMENT ON TABLE tr_svc_quality_ov IS '门诊服务质量分析-概览(门诊服务质量总览:投诉数量和表扬数量)';
COMMENT ON TABLE tr_svc_quality_cmpl IS '门诊服务质量分析-投诉明细(门诊投诉事件的详细记录)';
COMMENT ON TABLE tr_svc_quality_prz IS '门诊服务质量分析-表扬明细(门诊表扬事件的详细记录)';
COMMENT ON TABLE tr_spec_treat_ov IS '专科治疗量统计-概览(专科治疗量的总览数据:治疗人次、治疗金额、患者人数)';
COMMENT ON TABLE tr_spec_treat_dtl IS '专科治疗量统计-科室明细(按科室维度统计专科治疗量)';
COMMENT ON TABLE tr_win_stat_ov IS '人工窗口统计-概览(人工窗口业务量的总览指标:挂号/收费/退费人次)';
COMMENT ON TABLE tr_win_stat_age IS '人工窗口统计-年龄分析(按年龄段分析窗口业务分布)';
COMMENT ON TABLE tr_win_stat_tm IS '人工窗口统计-时段分析(按时段分析窗口业务量分布)';
COMMENT ON TABLE tr_win_stat_src IS '人工窗口统计-来源分析(按患者来源分析窗口业务分布)';
COMMENT ON TABLE tr_win_stat_load IS '人工窗口统计-工作量(按业务类型统计窗口工作量:挂号/收费/退费)';
COMMENT ON TABLE tr_cash_settle_ov IS '收费员结账统计-概览(收费员结账业务的总览指标:各类业务笔数及对比值)';
COMMENT ON TABLE tr_cash_settle_dtl IS '收费员结账统计-日明细(收费员每日结账的明细记录)';
COMMENT ON TABLE tr_cash_settle_cht IS '收费员结账统计-图表(收费员结账图表所需的结构化数据)';
COMMENT ON TABLE tr_disch_settle_ov IS '出院结算报表-概览(出院结算业务的总览指标:出院人次、结算金额)';
COMMENT ON TABLE tr_disch_settle_dtl IS '出院结算报表-日明细(出院结算按日期的详细对比数据:本期 vs 上期)';
COMMENT ON TABLE tr_disch_settle_cht IS '出院结算报表-图表(出院结算图表数据:按图表类型和项目分类存储)';

-- ============================================================
-- 添加字段注释
-- ============================================================

-- tr_outp_op_ov
COMMENT ON COLUMN tr_outp_op_ov.id IS '主键ID';
COMMENT ON COLUMN tr_outp_op_ov.stat_date IS '统计日期';
COMMENT ON COLUMN tr_outp_op_ov.total_visits IS '总就诊人次';
COMMENT ON COLUMN tr_outp_op_ov.appointment_rate IS '预约率';
COMMENT ON COLUMN tr_outp_op_ov.visit_count IS '就诊人次';
COMMENT ON COLUMN tr_outp_op_ov.exam_rate IS '检查率';
COMMENT ON COLUMN tr_outp_op_ov.efficiency IS '效率';
COMMENT ON COLUMN tr_outp_op_ov.effective_units IS '有效单元数';
COMMENT ON COLUMN tr_outp_op_ov.total_units IS '总单元数';
COMMENT ON COLUMN tr_outp_op_ov.famous_expert IS '名医就诊人次';
COMMENT ON COLUMN tr_outp_op_ov.special_expert IS '特需专家就诊人次';
COMMENT ON COLUMN tr_outp_op_ov.known_expert IS '知名专家就诊人次';
COMMENT ON COLUMN tr_outp_op_ov.expert_a IS '专家A就诊人次';
COMMENT ON COLUMN tr_outp_op_ov.expert_b IS '专家B就诊人次';
COMMENT ON COLUMN tr_outp_op_ov.ordinary IS '普通就诊人次';
COMMENT ON COLUMN tr_outp_op_ov.unit_famous_effective IS '名医有效单元';
COMMENT ON COLUMN tr_outp_op_ov.unit_famous_total IS '名医总单元';
COMMENT ON COLUMN tr_outp_op_ov.unit_special_effective IS '特需有效单元';
COMMENT ON COLUMN tr_outp_op_ov.unit_special_total IS '特需总单元';
COMMENT ON COLUMN tr_outp_op_ov.unit_known_effective IS '知名专家有效单元';
COMMENT ON COLUMN tr_outp_op_ov.unit_known_total IS '知名专家总单元';
COMMENT ON COLUMN tr_outp_op_ov.unit_a_effective IS '专家A有效单元';
COMMENT ON COLUMN tr_outp_op_ov.unit_a_total IS '专家A总单元';
COMMENT ON COLUMN tr_outp_op_ov.unit_b_effective IS '专家B有效单元';
COMMENT ON COLUMN tr_outp_op_ov.unit_b_total IS '专家B总单元';
COMMENT ON COLUMN tr_outp_op_ov.unit_ordinary_effective IS '普通有效单元';
COMMENT ON COLUMN tr_outp_op_ov.unit_ordinary_total IS '普通总单元';
COMMENT ON COLUMN tr_outp_op_ov.create_time IS '创建时间';
COMMENT ON COLUMN tr_outp_op_ov.update_time IS '更新时间';
COMMENT ON COLUMN tr_outp_op_ov.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_outp_op_ov.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_outp_op_ov.ext3 IS '扩展字段3';

-- tr_outp_op_dtl
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

-- tr_outp_alt_ov
COMMENT ON COLUMN tr_outp_alt_ov.id IS '主键ID';
COMMENT ON COLUMN tr_outp_alt_ov.stat_date IS '统计日期';
COMMENT ON COLUMN tr_outp_alt_ov.remain_alert IS '滞留预警';
COMMENT ON COLUMN tr_outp_alt_ov.appointment_alert IS '预约预警';
COMMENT ON COLUMN tr_outp_alt_ov.early_leave IS '早退人数';
COMMENT ON COLUMN tr_outp_alt_ov.create_time IS '创建时间';
COMMENT ON COLUMN tr_outp_alt_ov.update_time IS '更新时间';
COMMENT ON COLUMN tr_outp_alt_ov.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_outp_alt_ov.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_outp_alt_ov.ext3 IS '扩展字段3';

-- tr_outp_alt_dept
COMMENT ON COLUMN tr_outp_alt_dept.id IS '主键ID';
COMMENT ON COLUMN tr_outp_alt_dept.stat_date IS '统计日期';
COMMENT ON COLUMN tr_outp_alt_dept.dept_name IS '科室名称';
COMMENT ON COLUMN tr_outp_alt_dept.remain_alert IS '滞留预警';
COMMENT ON COLUMN tr_outp_alt_dept.appointment_alert IS '预约预警';
COMMENT ON COLUMN tr_outp_alt_dept.early_leave IS '早退人数';
COMMENT ON COLUMN tr_outp_alt_dept.create_time IS '创建时间';
COMMENT ON COLUMN tr_outp_alt_dept.update_time IS '更新时间';
COMMENT ON COLUMN tr_outp_alt_dept.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_outp_alt_dept.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_outp_alt_dept.ext3 IS '扩展字段3';

-- tr_outp_alt_doc
COMMENT ON COLUMN tr_outp_alt_doc.id IS '主键ID';
COMMENT ON COLUMN tr_outp_alt_doc.stat_date IS '统计日期';
COMMENT ON COLUMN tr_outp_alt_doc.doctor_name IS '医生姓名';
COMMENT ON COLUMN tr_outp_alt_doc.dept_name IS '科室名称';
COMMENT ON COLUMN tr_outp_alt_doc.remain_alert IS '滞留预警';
COMMENT ON COLUMN tr_outp_alt_doc.appointment_alert IS '预约预警';
COMMENT ON COLUMN tr_outp_alt_doc.early_leave IS '早退人数';
COMMENT ON COLUMN tr_outp_alt_doc.create_time IS '创建时间';
COMMENT ON COLUMN tr_outp_alt_doc.update_time IS '更新时间';
COMMENT ON COLUMN tr_outp_alt_doc.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_outp_alt_doc.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_outp_alt_doc.ext3 IS '扩展字段3';

-- tr_outp_fc_ov
COMMENT ON COLUMN tr_outp_fc_ov.id IS '主键ID';
COMMENT ON COLUMN tr_outp_fc_ov.stat_date IS '统计日期';
COMMENT ON COLUMN tr_outp_fc_ov.tomorrow IS '明日预测';
COMMENT ON COLUMN tr_outp_fc_ov.next_week IS '下周预测';
COMMENT ON COLUMN tr_outp_fc_ov.next_month IS '下月预测';
COMMENT ON COLUMN tr_outp_fc_ov.next_year IS '明年预测';
COMMENT ON COLUMN tr_outp_fc_ov.create_time IS '创建时间';
COMMENT ON COLUMN tr_outp_fc_ov.update_time IS '更新时间';
COMMENT ON COLUMN tr_outp_fc_ov.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_outp_fc_ov.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_outp_fc_ov.ext3 IS '扩展字段3';

-- tr_outp_fc_month
COMMENT ON COLUMN tr_outp_fc_month.id IS '主键ID';
COMMENT ON COLUMN tr_outp_fc_month.stat_date IS '统计日期';
COMMENT ON COLUMN tr_outp_fc_month.forecast_date IS '预测日期';
COMMENT ON COLUMN tr_outp_fc_month.forecast_value IS '预测值';
COMMENT ON COLUMN tr_outp_fc_month.create_time IS '创建时间';
COMMENT ON COLUMN tr_outp_fc_month.update_time IS '更新时间';
COMMENT ON COLUMN tr_outp_fc_month.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_outp_fc_month.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_outp_fc_month.ext3 IS '扩展字段3';

-- tr_outp_fc_year
COMMENT ON COLUMN tr_outp_fc_year.id IS '主键ID';
COMMENT ON COLUMN tr_outp_fc_year.stat_date IS '统计日期';
COMMENT ON COLUMN tr_outp_fc_year.forecast_month IS '预测月份(YYYY-MM)';
COMMENT ON COLUMN tr_outp_fc_year.forecast_value IS '预测值';
COMMENT ON COLUMN tr_outp_fc_year.create_time IS '创建时间';
COMMENT ON COLUMN tr_outp_fc_year.update_time IS '更新时间';
COMMENT ON COLUMN tr_outp_fc_year.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_outp_fc_year.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_outp_fc_year.ext3 IS '扩展字段3';

-- tr_inet_hosp_ov
COMMENT ON COLUMN tr_inet_hosp_ov.id IS '主键ID';
COMMENT ON COLUMN tr_inet_hosp_ov.stat_month IS '统计月份(YYYY-MM)';
COMMENT ON COLUMN tr_inet_hosp_ov.outpatient_volume IS '门诊量';
COMMENT ON COLUMN tr_inet_hosp_ov.doctor_ratio IS '医师占比';
COMMENT ON COLUMN tr_inet_hosp_ov.reception_rate IS '接诊率';
COMMENT ON COLUMN tr_inet_hosp_ov.prescription_rate IS '处方率';
COMMENT ON COLUMN tr_inet_hosp_ov.record_rate IS '病历率';
COMMENT ON COLUMN tr_inet_hosp_ov.review_rate IS '审方率';
COMMENT ON COLUMN tr_inet_hosp_ov.execution_rate IS '执行率';
COMMENT ON COLUMN tr_inet_hosp_ov.create_time IS '创建时间';
COMMENT ON COLUMN tr_inet_hosp_ov.update_time IS '更新时间';
COMMENT ON COLUMN tr_inet_hosp_ov.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_inet_hosp_ov.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_inet_hosp_ov.ext3 IS '扩展字段3';

-- tr_inet_hosp_op
COMMENT ON COLUMN tr_inet_hosp_op.id IS '主键ID';
COMMENT ON COLUMN tr_inet_hosp_op.stat_month IS '统计月份(YYYY-MM)';
COMMENT ON COLUMN tr_inet_hosp_op.item_name IS '指标名称';
COMMENT ON COLUMN tr_inet_hosp_op.current_value IS '当月值';
COMMENT ON COLUMN tr_inet_hosp_op.last_value IS '上月值';
COMMENT ON COLUMN tr_inet_hosp_op.growth_rate IS '增长率';
COMMENT ON COLUMN tr_inet_hosp_op.create_time IS '创建时间';
COMMENT ON COLUMN tr_inet_hosp_op.update_time IS '更新时间';
COMMENT ON COLUMN tr_inet_hosp_op.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_inet_hosp_op.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_inet_hosp_op.ext3 IS '扩展字段3';

-- tr_inet_hosp_biz
COMMENT ON COLUMN tr_inet_hosp_biz.id IS '主键ID';
COMMENT ON COLUMN tr_inet_hosp_biz.stat_month IS '统计月份(YYYY-MM)';
COMMENT ON COLUMN tr_inet_hosp_biz.category IS '分类';
COMMENT ON COLUMN tr_inet_hosp_biz.current_value IS '当月值';
COMMENT ON COLUMN tr_inet_hosp_biz.last_value IS '上月值';
COMMENT ON COLUMN tr_inet_hosp_biz.create_time IS '创建时间';
COMMENT ON COLUMN tr_inet_hosp_biz.update_time IS '更新时间';
COMMENT ON COLUMN tr_inet_hosp_biz.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_inet_hosp_biz.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_inet_hosp_biz.ext3 IS '扩展字段3';

-- tr_inet_hosp_dept_rnk
COMMENT ON COLUMN tr_inet_hosp_dept_rnk.id IS '主键ID';
COMMENT ON COLUMN tr_inet_hosp_dept_rnk.stat_month IS '统计月份(YYYY-MM)';
COMMENT ON COLUMN tr_inet_hosp_dept_rnk.rank_num IS '排名';
COMMENT ON COLUMN tr_inet_hosp_dept_rnk.dept_name IS '科室名称';
COMMENT ON COLUMN tr_inet_hosp_dept_rnk.current_month IS '当月值';
COMMENT ON COLUMN tr_inet_hosp_dept_rnk.last_month IS '上月值';
COMMENT ON COLUMN tr_inet_hosp_dept_rnk.growth_rate IS '增长率';
COMMENT ON COLUMN tr_inet_hosp_dept_rnk.create_time IS '创建时间';
COMMENT ON COLUMN tr_inet_hosp_dept_rnk.update_time IS '更新时间';
COMMENT ON COLUMN tr_inet_hosp_dept_rnk.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_inet_hosp_dept_rnk.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_inet_hosp_dept_rnk.ext3 IS '扩展字段3';

-- tr_inet_hosp_doc_rnk
COMMENT ON COLUMN tr_inet_hosp_doc_rnk.id IS '主键ID';
COMMENT ON COLUMN tr_inet_hosp_doc_rnk.stat_month IS '统计月份(YYYY-MM)';
COMMENT ON COLUMN tr_inet_hosp_doc_rnk.rank_num IS '排名';
COMMENT ON COLUMN tr_inet_hosp_doc_rnk.doctor_name IS '医生姓名';
COMMENT ON COLUMN tr_inet_hosp_doc_rnk.dept_name IS '科室名称';
COMMENT ON COLUMN tr_inet_hosp_doc_rnk.title IS '职称';
COMMENT ON COLUMN tr_inet_hosp_doc_rnk.current_month IS '当月值';
COMMENT ON COLUMN tr_inet_hosp_doc_rnk.create_time IS '创建时间';
COMMENT ON COLUMN tr_inet_hosp_doc_rnk.update_time IS '更新时间';
COMMENT ON COLUMN tr_inet_hosp_doc_rnk.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_inet_hosp_doc_rnk.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_inet_hosp_doc_rnk.ext3 IS '扩展字段3';

-- tr_inet_hosp_grw
COMMENT ON COLUMN tr_inet_hosp_grw.id IS '主键ID';
COMMENT ON COLUMN tr_inet_hosp_grw.stat_month IS '统计月份(YYYY-MM)';
COMMENT ON COLUMN tr_inet_hosp_grw.category IS '分类(月份)';
COMMENT ON COLUMN tr_inet_hosp_grw.data_value IS '数值';
COMMENT ON COLUMN tr_inet_hosp_grw.create_time IS '创建时间';
COMMENT ON COLUMN tr_inet_hosp_grw.update_time IS '更新时间';
COMMENT ON COLUMN tr_inet_hosp_grw.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_inet_hosp_grw.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_inet_hosp_grw.ext3 IS '扩展字段3';

-- tr_labstat_ov
COMMENT ON COLUMN tr_labstat_ov.id IS '主键ID';
COMMENT ON COLUMN tr_labstat_ov.stat_date IS '统计日期';
COMMENT ON COLUMN tr_labstat_ov.blood_collection IS '采血人次';
COMMENT ON COLUMN tr_labstat_ov.blood_efficiency IS '采血效率';
COMMENT ON COLUMN tr_labstat_ov.lab_efficiency IS '检验效率';
COMMENT ON COLUMN tr_labstat_ov.create_time IS '创建时间';
COMMENT ON COLUMN tr_labstat_ov.update_time IS '更新时间';
COMMENT ON COLUMN tr_labstat_ov.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_labstat_ov.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_labstat_ov.ext3 IS '扩展字段3';

-- tr_labstat_rnk
COMMENT ON COLUMN tr_labstat_rnk.id IS '主键ID';
COMMENT ON COLUMN tr_labstat_rnk.stat_date IS '统计日期';
COMMENT ON COLUMN tr_labstat_rnk.rank_type IS '排行类型(BLOOD/LAB)';
COMMENT ON COLUMN tr_labstat_rnk.rank_num IS '排名';
COMMENT ON COLUMN tr_labstat_rnk.item_name IS '项目名称';
COMMENT ON COLUMN tr_labstat_rnk.item_value IS '项目值';
COMMENT ON COLUMN tr_labstat_rnk.create_time IS '创建时间';
COMMENT ON COLUMN tr_labstat_rnk.update_time IS '更新时间';
COMMENT ON COLUMN tr_labstat_rnk.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_labstat_rnk.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_labstat_rnk.ext3 IS '扩展字段3';

-- tr_labstat_tm
COMMENT ON COLUMN tr_labstat_tm.id IS '主键ID';
COMMENT ON COLUMN tr_labstat_tm.stat_date IS '统计日期';
COMMENT ON COLUMN tr_labstat_tm.time_slot IS '时段';
COMMENT ON COLUMN tr_labstat_tm.blood_count IS '采血人次';
COMMENT ON COLUMN tr_labstat_tm.lab_count IS '检验人次';
COMMENT ON COLUMN tr_labstat_tm.create_time IS '创建时间';
COMMENT ON COLUMN tr_labstat_tm.update_time IS '更新时间';
COMMENT ON COLUMN tr_labstat_tm.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_labstat_tm.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_labstat_tm.ext3 IS '扩展字段3';

-- tr_medtech_ov
COMMENT ON COLUMN tr_medtech_ov.id IS '主键ID';
COMMENT ON COLUMN tr_medtech_ov.stat_date IS '统计日期';
COMMENT ON COLUMN tr_medtech_ov.check_count IS '检查人次';
COMMENT ON COLUMN tr_medtech_ov.on_time_rate IS '准时率';
COMMENT ON COLUMN tr_medtech_ov.wait_time IS '等候时长';
COMMENT ON COLUMN tr_medtech_ov.avg_wait_late IS '平均迟到';
COMMENT ON COLUMN tr_medtech_ov.avg_report_time IS '平均报告时长';
COMMENT ON COLUMN tr_medtech_ov.create_time IS '创建时间';
COMMENT ON COLUMN tr_medtech_ov.update_time IS '更新时间';
COMMENT ON COLUMN tr_medtech_ov.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_medtech_ov.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_medtech_ov.ext3 IS '扩展字段3';

-- tr_medtech_dtl
COMMENT ON COLUMN tr_medtech_dtl.id IS '主键ID';
COMMENT ON COLUMN tr_medtech_dtl.stat_date IS '统计日期';
COMMENT ON COLUMN tr_medtech_dtl.dept_name IS '科室名称';
COMMENT ON COLUMN tr_medtech_dtl.check_count IS '检查人次';
COMMENT ON COLUMN tr_medtech_dtl.on_time_rate IS '准时率';
COMMENT ON COLUMN tr_medtech_dtl.wait_time IS '等候时长';
COMMENT ON COLUMN tr_medtech_dtl.avg_wait_late IS '平均迟到';
COMMENT ON COLUMN tr_medtech_dtl.avg_report_time IS '平均报告时长';
COMMENT ON COLUMN tr_medtech_dtl.create_time IS '创建时间';
COMMENT ON COLUMN tr_medtech_dtl.update_time IS '更新时间';
COMMENT ON COLUMN tr_medtech_dtl.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_medtech_dtl.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_medtech_dtl.ext3 IS '扩展字段3';

-- tr_noshow_ov
COMMENT ON COLUMN tr_noshow_ov.id IS '主键ID';
COMMENT ON COLUMN tr_noshow_ov.stat_date IS '统计日期';
COMMENT ON COLUMN tr_noshow_ov.refund_count IS '退号人数';
COMMENT ON COLUMN tr_noshow_ov.refund_rate IS '退号率';
COMMENT ON COLUMN tr_noshow_ov.no_show_count IS '爽约人数';
COMMENT ON COLUMN tr_noshow_ov.no_show_rate IS '爽约率';
COMMENT ON COLUMN tr_noshow_ov.create_time IS '创建时间';
COMMENT ON COLUMN tr_noshow_ov.update_time IS '更新时间';
COMMENT ON COLUMN tr_noshow_ov.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_noshow_ov.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_noshow_ov.ext3 IS '扩展字段3';

-- tr_noshow_dtl
COMMENT ON COLUMN tr_noshow_dtl.id IS '主键ID';
COMMENT ON COLUMN tr_noshow_dtl.stat_date IS '统计日期';
COMMENT ON COLUMN tr_noshow_dtl.dept_name IS '科室名称';
COMMENT ON COLUMN tr_noshow_dtl.refund_count IS '退号人数';
COMMENT ON COLUMN tr_noshow_dtl.refund_rate IS '退号率';
COMMENT ON COLUMN tr_noshow_dtl.no_show_count IS '爽约人数';
COMMENT ON COLUMN tr_noshow_dtl.no_show_rate IS '爽约率';
COMMENT ON COLUMN tr_noshow_dtl.create_time IS '创建时间';
COMMENT ON COLUMN tr_noshow_dtl.update_time IS '更新时间';
COMMENT ON COLUMN tr_noshow_dtl.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_noshow_dtl.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_noshow_dtl.ext3 IS '扩展字段3';

-- tr_noshow_org
COMMENT ON COLUMN tr_noshow_org.id IS '主键ID';
COMMENT ON COLUMN tr_noshow_org.stat_date IS '统计日期';
COMMENT ON COLUMN tr_noshow_org.origin_type IS '来源类型(REFUND/NO_SHOW)';
COMMENT ON COLUMN tr_noshow_org.item_name IS '来源名称';
COMMENT ON COLUMN tr_noshow_org.item_value IS '数量';
COMMENT ON COLUMN tr_noshow_org.create_time IS '创建时间';
COMMENT ON COLUMN tr_noshow_org.update_time IS '更新时间';
COMMENT ON COLUMN tr_noshow_org.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_noshow_org.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_noshow_org.ext3 IS '扩展字段3';

-- tr_noshow_chn
COMMENT ON COLUMN tr_noshow_chn.id IS '主键ID';
COMMENT ON COLUMN tr_noshow_chn.stat_date IS '统计日期';
COMMENT ON COLUMN tr_noshow_chn.channel_type IS '渠道类型(REFUND/NO_SHOW)';
COMMENT ON COLUMN tr_noshow_chn.item_name IS '渠道名称';
COMMENT ON COLUMN tr_noshow_chn.item_value IS '数量';
COMMENT ON COLUMN tr_noshow_chn.create_time IS '创建时间';
COMMENT ON COLUMN tr_noshow_chn.update_time IS '更新时间';
COMMENT ON COLUMN tr_noshow_chn.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_noshow_chn.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_noshow_chn.ext3 IS '扩展字段3';

-- tr_noshow_age
COMMENT ON COLUMN tr_noshow_age.id IS '主键ID';
COMMENT ON COLUMN tr_noshow_age.stat_date IS '统计日期';
COMMENT ON COLUMN tr_noshow_age.age_group IS '年龄段';
COMMENT ON COLUMN tr_noshow_age.no_show_count IS '爽约人数';
COMMENT ON COLUMN tr_noshow_age.refund_count IS '退号人数';
COMMENT ON COLUMN tr_noshow_age.create_time IS '创建时间';
COMMENT ON COLUMN tr_noshow_age.update_time IS '更新时间';
COMMENT ON COLUMN tr_noshow_age.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_noshow_age.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_noshow_age.ext3 IS '扩展字段3';

-- tr_pat_portrait_age
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

-- tr_pat_portrait_insur
COMMENT ON COLUMN tr_pat_portrait_insur.id IS '主键ID';
COMMENT ON COLUMN tr_pat_portrait_insur.stat_date IS '统计日期';
COMMENT ON COLUMN tr_pat_portrait_insur.insurance_type IS '医保类型';
COMMENT ON COLUMN tr_pat_portrait_insur.patient_count IS '患者人数';
COMMENT ON COLUMN tr_pat_portrait_insur.create_time IS '创建时间';
COMMENT ON COLUMN tr_pat_portrait_insur.update_time IS '更新时间';
COMMENT ON COLUMN tr_pat_portrait_insur.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_pat_portrait_insur.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_pat_portrait_insur.ext3 IS '扩展字段3';

-- tr_pat_portrait_idty
COMMENT ON COLUMN tr_pat_portrait_idty.id IS '主键ID';
COMMENT ON COLUMN tr_pat_portrait_idty.stat_date IS '统计日期';
COMMENT ON COLUMN tr_pat_portrait_idty.identity_type IS '身份类型';
COMMENT ON COLUMN tr_pat_portrait_idty.patient_count IS '患者人数';
COMMENT ON COLUMN tr_pat_portrait_idty.create_time IS '创建时间';
COMMENT ON COLUMN tr_pat_portrait_idty.update_time IS '更新时间';
COMMENT ON COLUMN tr_pat_portrait_idty.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_pat_portrait_idty.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_pat_portrait_idty.ext3 IS '扩展字段3';

-- tr_pat_portrait_reg
COMMENT ON COLUMN tr_pat_portrait_reg.id IS '主键ID';
COMMENT ON COLUMN tr_pat_portrait_reg.stat_date IS '统计日期';
COMMENT ON COLUMN tr_pat_portrait_reg.source_type IS '来源类型';
COMMENT ON COLUMN tr_pat_portrait_reg.patient_count IS '患者人数';
COMMENT ON COLUMN tr_pat_portrait_reg.create_time IS '创建时间';
COMMENT ON COLUMN tr_pat_portrait_reg.update_time IS '更新时间';
COMMENT ON COLUMN tr_pat_portrait_reg.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_pat_portrait_reg.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_pat_portrait_reg.ext3 IS '扩展字段3';

-- tr_pat_portrait_arc
COMMENT ON COLUMN tr_pat_portrait_arc.id IS '主键ID';
COMMENT ON COLUMN tr_pat_portrait_arc.stat_date IS '统计日期';
COMMENT ON COLUMN tr_pat_portrait_arc.source_type IS '来源类型';
COMMENT ON COLUMN tr_pat_portrait_arc.patient_count IS '患者人数';
COMMENT ON COLUMN tr_pat_portrait_arc.create_time IS '创建时间';
COMMENT ON COLUMN tr_pat_portrait_arc.update_time IS '更新时间';
COMMENT ON COLUMN tr_pat_portrait_arc.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_pat_portrait_arc.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_pat_portrait_arc.ext3 IS '扩展字段3';

-- tr_qc_ov
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

-- tr_qc_dtl
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

-- tr_rev_ov
COMMENT ON COLUMN tr_rev_ov.id IS '主键ID';
COMMENT ON COLUMN tr_rev_ov.stat_date IS '统计日期';
COMMENT ON COLUMN tr_rev_ov.outpatient_revenue IS '门诊收入';
COMMENT ON COLUMN tr_rev_ov.service_revenue IS '服务收入';
COMMENT ON COLUMN tr_rev_ov.create_time IS '创建时间';
COMMENT ON COLUMN tr_rev_ov.update_time IS '更新时间';
COMMENT ON COLUMN tr_rev_ov.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_rev_ov.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_rev_ov.ext3 IS '扩展字段3';

-- tr_rev_dept
COMMENT ON COLUMN tr_rev_dept.id IS '主键ID';
COMMENT ON COLUMN tr_rev_dept.stat_date IS '统计日期';
COMMENT ON COLUMN tr_rev_dept.dept_name IS '科室名称';
COMMENT ON COLUMN tr_rev_dept.outpatient_revenue IS '门诊收入';
COMMENT ON COLUMN tr_rev_dept.service_revenue IS '服务收入';
COMMENT ON COLUMN tr_rev_dept.create_time IS '创建时间';
COMMENT ON COLUMN tr_rev_dept.update_time IS '更新时间';
COMMENT ON COLUMN tr_rev_dept.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_rev_dept.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_rev_dept.ext3 IS '扩展字段3';

-- tr_rev_doc
COMMENT ON COLUMN tr_rev_doc.id IS '主键ID';
COMMENT ON COLUMN tr_rev_doc.stat_date IS '统计日期';
COMMENT ON COLUMN tr_rev_doc.doctor_name IS '医生姓名';
COMMENT ON COLUMN tr_rev_doc.dept_name IS '科室名称';
COMMENT ON COLUMN tr_rev_doc.doctor_benefit IS '医生收益';
COMMENT ON COLUMN tr_rev_doc.service_revenue IS '服务收入';
COMMENT ON COLUMN tr_rev_doc.create_time IS '创建时间';
COMMENT ON COLUMN tr_rev_doc.update_time IS '更新时间';
COMMENT ON COLUMN tr_rev_doc.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_rev_doc.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_rev_doc.ext3 IS '扩展字段3';

-- tr_room_use_ov
COMMENT ON COLUMN tr_room_use_ov.id IS '主键ID';
COMMENT ON COLUMN tr_room_use_ov.stat_date IS '统计日期';
COMMENT ON COLUMN tr_room_use_ov.avg_usage IS '平均使用率';
COMMENT ON COLUMN tr_room_use_ov.am_usage IS '上午使用率';
COMMENT ON COLUMN tr_room_use_ov.pm_usage IS '下午使用率';
COMMENT ON COLUMN tr_room_use_ov.holiday_usage IS '节假日使用率';
COMMENT ON COLUMN tr_room_use_ov.create_time IS '创建时间';
COMMENT ON COLUMN tr_room_use_ov.update_time IS '更新时间';
COMMENT ON COLUMN tr_room_use_ov.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_room_use_ov.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_room_use_ov.ext3 IS '扩展字段3';

-- tr_room_use_dtl
COMMENT ON COLUMN tr_room_use_dtl.id IS '主键ID';
COMMENT ON COLUMN tr_room_use_dtl.stat_date IS '统计日期';
COMMENT ON COLUMN tr_room_use_dtl.dept_name IS '科室名称';
COMMENT ON COLUMN tr_room_use_dtl.avg_usage IS '平均使用率';
COMMENT ON COLUMN tr_room_use_dtl.am_usage IS '上午使用率';
COMMENT ON COLUMN tr_room_use_dtl.pm_usage IS '下午使用率';
COMMENT ON COLUMN tr_room_use_dtl.holiday_usage IS '节假日使用率';
COMMENT ON COLUMN tr_room_use_dtl.create_time IS '创建时间';
COMMENT ON COLUMN tr_room_use_dtl.update_time IS '更新时间';
COMMENT ON COLUMN tr_room_use_dtl.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_room_use_dtl.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_room_use_dtl.ext3 IS '扩展字段3';

-- tr_svc_quality_ov
COMMENT ON COLUMN tr_svc_quality_ov.id IS '主键ID';
COMMENT ON COLUMN tr_svc_quality_ov.stat_date IS '统计日期';
COMMENT ON COLUMN tr_svc_quality_ov.complaint_count IS '投诉数量';
COMMENT ON COLUMN tr_svc_quality_ov.praise_count IS '表扬数量';
COMMENT ON COLUMN tr_svc_quality_ov.create_time IS '创建时间';
COMMENT ON COLUMN tr_svc_quality_ov.update_time IS '更新时间';
COMMENT ON COLUMN tr_svc_quality_ov.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_svc_quality_ov.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_svc_quality_ov.ext3 IS '扩展字段3';

-- tr_svc_quality_cmpl
COMMENT ON COLUMN tr_svc_quality_cmpl.id IS '主键ID';
COMMENT ON COLUMN tr_svc_quality_cmpl.stat_date IS '统计日期';
COMMENT ON COLUMN tr_svc_quality_cmpl.complaint_time IS '投诉时间';
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

-- tr_svc_quality_prz
COMMENT ON COLUMN tr_svc_quality_prz.id IS '主键ID';
COMMENT ON COLUMN tr_svc_quality_prz.stat_date IS '统计日期';
COMMENT ON COLUMN tr_svc_quality_prz.praise_time IS '表扬时间';
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

-- tr_spec_treat_ov
COMMENT ON COLUMN tr_spec_treat_ov.id IS '主键ID';
COMMENT ON COLUMN tr_spec_treat_ov.stat_date IS '统计日期';
COMMENT ON COLUMN tr_spec_treat_ov.treatment_count IS '治疗人次';
COMMENT ON COLUMN tr_spec_treat_ov.treatment_amount IS '治疗金额';
COMMENT ON COLUMN tr_spec_treat_ov.patient_count IS '患者人数';
COMMENT ON COLUMN tr_spec_treat_ov.create_time IS '创建时间';
COMMENT ON COLUMN tr_spec_treat_ov.update_time IS '更新时间';
COMMENT ON COLUMN tr_spec_treat_ov.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_spec_treat_ov.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_spec_treat_ov.ext3 IS '扩展字段3';

-- tr_spec_treat_dtl
COMMENT ON COLUMN tr_spec_treat_dtl.id IS '主键ID';
COMMENT ON COLUMN tr_spec_treat_dtl.stat_date IS '统计日期';
COMMENT ON COLUMN tr_spec_treat_dtl.dept_name IS '科室名称';
COMMENT ON COLUMN tr_spec_treat_dtl.treatment_count IS '治疗人次';
COMMENT ON COLUMN tr_spec_treat_dtl.treatment_amount IS '治疗金额';
COMMENT ON COLUMN tr_spec_treat_dtl.patient_count IS '患者人数';
COMMENT ON COLUMN tr_spec_treat_dtl.create_time IS '创建时间';
COMMENT ON COLUMN tr_spec_treat_dtl.update_time IS '更新时间';
COMMENT ON COLUMN tr_spec_treat_dtl.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_spec_treat_dtl.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_spec_treat_dtl.ext3 IS '扩展字段3';

-- tr_win_stat_ov
COMMENT ON COLUMN tr_win_stat_ov.id IS '主键ID';
COMMENT ON COLUMN tr_win_stat_ov.stat_date IS '统计日期';
COMMENT ON COLUMN tr_win_stat_ov.register_count IS '挂号人次';
COMMENT ON COLUMN tr_win_stat_ov.payment_count IS '收费人次';
COMMENT ON COLUMN tr_win_stat_ov.refund_count IS '退费人次';
COMMENT ON COLUMN tr_win_stat_ov.create_time IS '创建时间';
COMMENT ON COLUMN tr_win_stat_ov.update_time IS '更新时间';
COMMENT ON COLUMN tr_win_stat_ov.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_win_stat_ov.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_win_stat_ov.ext3 IS '扩展字段3';

-- tr_win_stat_age
COMMENT ON COLUMN tr_win_stat_age.id IS '主键ID';
COMMENT ON COLUMN tr_win_stat_age.stat_date IS '统计日期';
COMMENT ON COLUMN tr_win_stat_age.age_group IS '年龄段';
COMMENT ON COLUMN tr_win_stat_age.patient_count IS '人数';
COMMENT ON COLUMN tr_win_stat_age.create_time IS '创建时间';
COMMENT ON COLUMN tr_win_stat_age.update_time IS '更新时间';
COMMENT ON COLUMN tr_win_stat_age.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_win_stat_age.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_win_stat_age.ext3 IS '扩展字段3';

-- tr_win_stat_tm
COMMENT ON COLUMN tr_win_stat_tm.id IS '主键ID';
COMMENT ON COLUMN tr_win_stat_tm.stat_date IS '统计日期';
COMMENT ON COLUMN tr_win_stat_tm.time_slot IS '时段';
COMMENT ON COLUMN tr_win_stat_tm.business_count IS '业务量';
COMMENT ON COLUMN tr_win_stat_tm.create_time IS '创建时间';
COMMENT ON COLUMN tr_win_stat_tm.update_time IS '更新时间';
COMMENT ON COLUMN tr_win_stat_tm.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_win_stat_tm.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_win_stat_tm.ext3 IS '扩展字段3';

-- tr_win_stat_src
COMMENT ON COLUMN tr_win_stat_src.id IS '主键ID';
COMMENT ON COLUMN tr_win_stat_src.stat_date IS '统计日期';
COMMENT ON COLUMN tr_win_stat_src.source_name IS '来源名称';
COMMENT ON COLUMN tr_win_stat_src.source_count IS '数量';
COMMENT ON COLUMN tr_win_stat_src.create_time IS '创建时间';
COMMENT ON COLUMN tr_win_stat_src.update_time IS '更新时间';
COMMENT ON COLUMN tr_win_stat_src.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_win_stat_src.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_win_stat_src.ext3 IS '扩展字段3';

-- tr_win_stat_load
COMMENT ON COLUMN tr_win_stat_load.id IS '主键ID';
COMMENT ON COLUMN tr_win_stat_load.stat_date IS '统计日期';
COMMENT ON COLUMN tr_win_stat_load.business_type IS '业务类型';
COMMENT ON COLUMN tr_win_stat_load.register_count IS '挂号数';
COMMENT ON COLUMN tr_win_stat_load.payment_count IS '收费数';
COMMENT ON COLUMN tr_win_stat_load.refund_count IS '退费数';
COMMENT ON COLUMN tr_win_stat_load.create_time IS '创建时间';
COMMENT ON COLUMN tr_win_stat_load.update_time IS '更新时间';
COMMENT ON COLUMN tr_win_stat_load.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_win_stat_load.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_win_stat_load.ext3 IS '扩展字段3';

-- tr_cash_settle_ov
COMMENT ON COLUMN tr_cash_settle_ov.id IS '主键ID';
COMMENT ON COLUMN tr_cash_settle_ov.stat_date IS '统计日期';
COMMENT ON COLUMN tr_cash_settle_ov.appointment_register IS '预约挂号';
COMMENT ON COLUMN tr_cash_settle_ov.appointment_register_compare IS '预约挂号对比';
COMMENT ON COLUMN tr_cash_settle_ov.appointment_fetch IS '预约取号';
COMMENT ON COLUMN tr_cash_settle_ov.appointment_fetch_compare IS '预约取号对比';
COMMENT ON COLUMN tr_cash_settle_ov.today_register IS '当日挂号';
COMMENT ON COLUMN tr_cash_settle_ov.today_register_compare IS '当日挂号对比';
COMMENT ON COLUMN tr_cash_settle_ov.refund IS '退号';
COMMENT ON COLUMN tr_cash_settle_ov.refund_compare IS '退号对比';
COMMENT ON COLUMN tr_cash_settle_ov.outpatient_charge IS '门诊收费';
COMMENT ON COLUMN tr_cash_settle_ov.outpatient_charge_compare IS '门诊收费对比';
COMMENT ON COLUMN tr_cash_settle_ov.outpatient_refund IS '门诊退费';
COMMENT ON COLUMN tr_cash_settle_ov.outpatient_refund_compare IS '门诊退费对比';
COMMENT ON COLUMN tr_cash_settle_ov.prepayment IS '预交金';
COMMENT ON COLUMN tr_cash_settle_ov.prepayment_compare IS '预交金对比';
COMMENT ON COLUMN tr_cash_settle_ov.hospital_refund IS '住院退费';
COMMENT ON COLUMN tr_cash_settle_ov.hospital_refund_compare IS '住院退费对比';
COMMENT ON COLUMN tr_cash_settle_ov.discharge_settlement IS '出院结算';
COMMENT ON COLUMN tr_cash_settle_ov.discharge_settlement_compare IS '出院结算对比';
COMMENT ON COLUMN tr_cash_settle_ov.create_time IS '创建时间';
COMMENT ON COLUMN tr_cash_settle_ov.update_time IS '更新时间';
COMMENT ON COLUMN tr_cash_settle_ov.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_cash_settle_ov.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_cash_settle_ov.ext3 IS '扩展字段3';

-- tr_cash_settle_dtl
COMMENT ON COLUMN tr_cash_settle_dtl.id IS '主键ID';
COMMENT ON COLUMN tr_cash_settle_dtl.stat_date IS '统计日期';
COMMENT ON COLUMN tr_cash_settle_dtl.item_date IS '日期';
COMMENT ON COLUMN tr_cash_settle_dtl.cashier_name IS '收费员';
COMMENT ON COLUMN tr_cash_settle_dtl.item_type IS '项目类型';
COMMENT ON COLUMN tr_cash_settle_dtl.item_value IS '金额';
COMMENT ON COLUMN tr_cash_settle_dtl.create_time IS '创建时间';
COMMENT ON COLUMN tr_cash_settle_dtl.update_time IS '更新时间';
COMMENT ON COLUMN tr_cash_settle_dtl.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_cash_settle_dtl.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_cash_settle_dtl.ext3 IS '扩展字段3';

-- tr_cash_settle_cht
COMMENT ON COLUMN tr_cash_settle_cht.id IS '主键ID';
COMMENT ON COLUMN tr_cash_settle_cht.stat_date IS '统计日期';
COMMENT ON COLUMN tr_cash_settle_cht.chart_title IS '图表标题';
COMMENT ON COLUMN tr_cash_settle_cht.chart_subtitle IS '副标题';
COMMENT ON COLUMN tr_cash_settle_cht.date_range IS '日期范围';
COMMENT ON COLUMN tr_cash_settle_cht.category IS '分类';
COMMENT ON COLUMN tr_cash_settle_cht.data_value IS '数值';
COMMENT ON COLUMN tr_cash_settle_cht.create_time IS '创建时间';
COMMENT ON COLUMN tr_cash_settle_cht.update_time IS '更新时间';
COMMENT ON COLUMN tr_cash_settle_cht.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_cash_settle_cht.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_cash_settle_cht.ext3 IS '扩展字段3';

-- tr_disch_settle_ov
COMMENT ON COLUMN tr_disch_settle_ov.id IS '主键ID';
COMMENT ON COLUMN tr_disch_settle_ov.stat_date IS '统计日期';
COMMENT ON COLUMN tr_disch_settle_ov.total_discharge_count IS '总出院人次';
COMMENT ON COLUMN tr_disch_settle_ov.total_discharge_compare IS '总出院对比';
COMMENT ON COLUMN tr_disch_settle_ov.discharged_count IS '已出院人次';
COMMENT ON COLUMN tr_disch_settle_ov.discharged_compare IS '已出院对比';
COMMENT ON COLUMN tr_disch_settle_ov.not_discharged_count IS '未出院人次';
COMMENT ON COLUMN tr_disch_settle_ov.not_discharged_compare IS '未出院对比';
COMMENT ON COLUMN tr_disch_settle_ov.settlement_amount IS '结算金额';
COMMENT ON COLUMN tr_disch_settle_ov.settlement_amount_compare IS '结算金额对比';
COMMENT ON COLUMN tr_disch_settle_ov.create_time IS '创建时间';
COMMENT ON COLUMN tr_disch_settle_ov.update_time IS '更新时间';
COMMENT ON COLUMN tr_disch_settle_ov.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_disch_settle_ov.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_disch_settle_ov.ext3 IS '扩展字段3';

-- tr_disch_settle_dtl
COMMENT ON COLUMN tr_disch_settle_dtl.id IS '主键ID';
COMMENT ON COLUMN tr_disch_settle_dtl.stat_date IS '统计日期';
COMMENT ON COLUMN tr_disch_settle_dtl.item_date IS '日期';
COMMENT ON COLUMN tr_disch_settle_dtl.total_last IS '总出院上期';
COMMENT ON COLUMN tr_disch_settle_dtl.total_current IS '总出院本期';
COMMENT ON COLUMN tr_disch_settle_dtl.total_compare IS '总出院对比';
COMMENT ON COLUMN tr_disch_settle_dtl.discharged_last IS '已出院上期';
COMMENT ON COLUMN tr_disch_settle_dtl.discharged_current IS '已出院本期';
COMMENT ON COLUMN tr_disch_settle_dtl.discharged_compare IS '已出院对比';
COMMENT ON COLUMN tr_disch_settle_dtl.not_discharged_last IS '未出院上期';
COMMENT ON COLUMN tr_disch_settle_dtl.not_discharged_current IS '未出院本期';
COMMENT ON COLUMN tr_disch_settle_dtl.not_discharged_compare IS '未出院对比';
COMMENT ON COLUMN tr_disch_settle_dtl.amount_last IS '金额上期';
COMMENT ON COLUMN tr_disch_settle_dtl.amount_current IS '金额本期';
COMMENT ON COLUMN tr_disch_settle_dtl.amount_compare IS '金额对比';
COMMENT ON COLUMN tr_disch_settle_dtl.create_time IS '创建时间';
COMMENT ON COLUMN tr_disch_settle_dtl.update_time IS '更新时间';
COMMENT ON COLUMN tr_disch_settle_dtl.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_disch_settle_dtl.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_disch_settle_dtl.ext3 IS '扩展字段3';

-- tr_disch_settle_cht
COMMENT ON COLUMN tr_disch_settle_cht.id IS '主键ID';
COMMENT ON COLUMN tr_disch_settle_cht.stat_date IS '统计日期';
COMMENT ON COLUMN tr_disch_settle_cht.chart_type IS '图表类型(CHANNEL/PATIENT_TYPE/AMOUNT_TYPE)';
COMMENT ON COLUMN tr_disch_settle_cht.item_name IS '项目名称';
COMMENT ON COLUMN tr_disch_settle_cht.item_value IS '数值';
COMMENT ON COLUMN tr_disch_settle_cht.item_compare IS '对比值';
COMMENT ON COLUMN tr_disch_settle_cht.create_time IS '创建时间';
COMMENT ON COLUMN tr_disch_settle_cht.update_time IS '更新时间';
COMMENT ON COLUMN tr_disch_settle_cht.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_disch_settle_cht.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_disch_settle_cht.ext3 IS '扩展字段3';

-- ============================================================
-- 17. 治疗统计报表
-- ============================================================

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
CREATE TABLE tr_treat_stat_dtl (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    dept_name       VARCHAR2(100)   NOT NULL,           -- 科室名称
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

-- ============================================================
-- 18. 住院预交金统计
-- ============================================================

-- 18.1 住院预交金概览表
CREATE TABLE tr_inpat_prepay_ov (
    id                      NUMBER(19)      PRIMARY KEY,
    stat_date               DATE            NOT NULL,   -- 统计日期
    prepayment_count        NUMBER(10)      DEFAULT 0,  -- 预交金笔数
    prepayment_count_compare NUMBER(10)     DEFAULT 0,  -- 预交金笔数对比
    prepayment_amount       NUMBER(18,2),               -- 预交金金额
    prepayment_amount_compare NUMBER(10)    DEFAULT 0,  -- 预交金金额对比
    create_time             DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time             DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1                    VARCHAR2(500),                        -- 扩展字段1
    ext2                    VARCHAR2(500),                        -- 扩展字段2
    ext3                    VARCHAR2(500)                         -- 扩展字段3
);

-- 18.2 住院预交金日明细表
CREATE TABLE tr_inpat_prepay_dtl (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    item_date       DATE            NOT NULL,           -- 日期
    data_type       VARCHAR2(20)    NOT NULL,           -- 数据类型(SUMMARY/INCOME/REFUND)
    count_last      NUMBER(10)      DEFAULT 0,          -- 上期笔数
    count_current   NUMBER(10)      DEFAULT 0,          -- 本期笔数
    count_compare   NUMBER(10)      DEFAULT 0,          -- 笔数对比
    amount_last     NUMBER(18,2),                       -- 上期金额
    amount_current  NUMBER(18,2),                       -- 本期金额
    amount_compare  NUMBER(10)      DEFAULT 0,          -- 金额对比
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 18.3 住院预交金图表数据表
CREATE TABLE tr_inpat_prepay_cht (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    chart_type      VARCHAR2(50)    NOT NULL,           -- 图表类型(TREND/CHANNEL/PAY_TYPE)
    chart_title     VARCHAR2(200),                      -- 图表标题
    chart_subtitle  VARCHAR2(200),                      -- 副标题
    date_range      VARCHAR2(100),                      -- 日期范围
    category        VARCHAR2(100)   NOT NULL,           -- 分类
    series_name     VARCHAR2(100),                      -- 系列名称
    data_value      NUMBER(10)      DEFAULT 0,          -- 数值
    compare_value   NUMBER(10)      DEFAULT 0,          -- 对比值
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- ============================================================
-- 表注释(新模块)
-- ============================================================

COMMENT ON TABLE tr_treat_stat_ov IS '治疗统计报表-概览(存储治疗统计总览:患者人数、治疗人次、治疗金额)';
COMMENT ON TABLE tr_treat_stat_dtl IS '治疗统计报表-科室明细(按科室维度统计治疗人次、金额、患者人数)';
COMMENT ON TABLE tr_treat_stat_trend IS '治疗统计报表-每日趋势(存储每日治疗量趋势数据,用于趋势图展示)';
COMMENT ON TABLE tr_inpat_prepay_ov IS '住院预交金统计-概览(存储住院预交金总览:预交金笔数、金额及对比值)';
COMMENT ON TABLE tr_inpat_prepay_dtl IS '住院预交金统计-日明细(按日期和数据类型存储预交金笔数和金额的明细对比)';
COMMENT ON TABLE tr_inpat_prepay_cht IS '住院预交金统计-图表(存储预交金趋势、渠道、支付方式等图表数据)';

-- ============================================================
-- 字段注释(新模块)
-- ============================================================

-- tr_treat_stat_ov
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

-- tr_treat_stat_dtl
COMMENT ON COLUMN tr_treat_stat_dtl.id IS '主键ID';
COMMENT ON COLUMN tr_treat_stat_dtl.stat_date IS '统计日期';
COMMENT ON COLUMN tr_treat_stat_dtl.dept_name IS '科室名称';
COMMENT ON COLUMN tr_treat_stat_dtl.patient_count IS '患者人数';
COMMENT ON COLUMN tr_treat_stat_dtl.treatment_count IS '治疗人次';
COMMENT ON COLUMN tr_treat_stat_dtl.treatment_amount IS '治疗金额';
COMMENT ON COLUMN tr_treat_stat_dtl.create_time IS '创建时间';
COMMENT ON COLUMN tr_treat_stat_dtl.update_time IS '更新时间';
COMMENT ON COLUMN tr_treat_stat_dtl.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_treat_stat_dtl.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_treat_stat_dtl.ext3 IS '扩展字段3';

-- tr_treat_stat_trend
COMMENT ON COLUMN tr_treat_stat_trend.id IS '主键ID';
COMMENT ON COLUMN tr_treat_stat_trend.stat_date IS '统计日期';
COMMENT ON COLUMN tr_treat_stat_trend.trend_date IS '趋势日期';
COMMENT ON COLUMN tr_treat_stat_trend.trend_value IS '趋势值';
COMMENT ON COLUMN tr_treat_stat_trend.create_time IS '创建时间';
COMMENT ON COLUMN tr_treat_stat_trend.update_time IS '更新时间';
COMMENT ON COLUMN tr_treat_stat_trend.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_treat_stat_trend.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_treat_stat_trend.ext3 IS '扩展字段3';

-- tr_inpat_prepay_ov
COMMENT ON COLUMN tr_inpat_prepay_ov.id IS '主键ID';
COMMENT ON COLUMN tr_inpat_prepay_ov.stat_date IS '统计日期';
COMMENT ON COLUMN tr_inpat_prepay_ov.prepayment_count IS '预交金笔数';
COMMENT ON COLUMN tr_inpat_prepay_ov.prepayment_count_compare IS '预交金笔数对比值';
COMMENT ON COLUMN tr_inpat_prepay_ov.prepayment_amount IS '预交金金额';
COMMENT ON COLUMN tr_inpat_prepay_ov.prepayment_amount_compare IS '预交金金额对比值';
COMMENT ON COLUMN tr_inpat_prepay_ov.create_time IS '创建时间';
COMMENT ON COLUMN tr_inpat_prepay_ov.update_time IS '更新时间';
COMMENT ON COLUMN tr_inpat_prepay_ov.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_inpat_prepay_ov.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_inpat_prepay_ov.ext3 IS '扩展字段3';

-- tr_inpat_prepay_dtl
COMMENT ON COLUMN tr_inpat_prepay_dtl.id IS '主键ID';
COMMENT ON COLUMN tr_inpat_prepay_dtl.stat_date IS '统计日期';
COMMENT ON COLUMN tr_inpat_prepay_dtl.item_date IS '日期';
COMMENT ON COLUMN tr_inpat_prepay_dtl.data_type IS '数据类型(SUMMARY/INCOME/REFUND)';
COMMENT ON COLUMN tr_inpat_prepay_dtl.count_last IS '上期笔数';
COMMENT ON COLUMN tr_inpat_prepay_dtl.count_current IS '本期笔数';
COMMENT ON COLUMN tr_inpat_prepay_dtl.count_compare IS '笔数对比';
COMMENT ON COLUMN tr_inpat_prepay_dtl.amount_last IS '上期金额';
COMMENT ON COLUMN tr_inpat_prepay_dtl.amount_current IS '本期金额';
COMMENT ON COLUMN tr_inpat_prepay_dtl.amount_compare IS '金额对比';
COMMENT ON COLUMN tr_inpat_prepay_dtl.create_time IS '创建时间';
COMMENT ON COLUMN tr_inpat_prepay_dtl.update_time IS '更新时间';
COMMENT ON COLUMN tr_inpat_prepay_dtl.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_inpat_prepay_dtl.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_inpat_prepay_dtl.ext3 IS '扩展字段3';

-- tr_inpat_prepay_cht
COMMENT ON COLUMN tr_inpat_prepay_cht.id IS '主键ID';
COMMENT ON COLUMN tr_inpat_prepay_cht.stat_date IS '统计日期';
COMMENT ON COLUMN tr_inpat_prepay_cht.chart_type IS '图表类型(TREND/CHANNEL/PAY_TYPE)';
COMMENT ON COLUMN tr_inpat_prepay_cht.chart_title IS '图表标题';
COMMENT ON COLUMN tr_inpat_prepay_cht.chart_subtitle IS '副标题';
COMMENT ON COLUMN tr_inpat_prepay_cht.date_range IS '日期范围';
COMMENT ON COLUMN tr_inpat_prepay_cht.category IS '分类';
COMMENT ON COLUMN tr_inpat_prepay_cht.series_name IS '系列名称';
COMMENT ON COLUMN tr_inpat_prepay_cht.data_value IS '数值';
COMMENT ON COLUMN tr_inpat_prepay_cht.compare_value IS '对比值';
COMMENT ON COLUMN tr_inpat_prepay_cht.create_time IS '创建时间';
COMMENT ON COLUMN tr_inpat_prepay_cht.update_time IS '更新时间';
COMMENT ON COLUMN tr_inpat_prepay_cht.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_inpat_prepay_cht.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_inpat_prepay_cht.ext3 IS '扩展字段3';

-- ============================================================
-- SQL 文件结束
-- ============================================================
