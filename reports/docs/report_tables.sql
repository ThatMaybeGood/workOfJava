-- ============================================================
-- 报表平台数据库表结构 - Oracle
-- 表名前缀：tr_ (table report)
-- 每个表预留3个扩展字段：ext1, ext2, ext3
-- 生成日期：2026-06-16
-- ============================================================

-- ============================================================
-- 1. 门诊运行数据统计
-- ============================================================

-- 1.1 门诊运行概览表
CREATE TABLE tr_outpatient_operation_overview (
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
    unit_famous_effective NUMBER(10) DEFAULT 0,
    unit_famous_total     NUMBER(10) DEFAULT 0,
    unit_special_effective NUMBER(10) DEFAULT 0,
    unit_special_total     NUMBER(10) DEFAULT 0,
    unit_known_effective   NUMBER(10) DEFAULT 0,
    unit_known_total       NUMBER(10) DEFAULT 0,
    unit_a_effective       NUMBER(10) DEFAULT 0,
    unit_a_total           NUMBER(10) DEFAULT 0,
    unit_b_effective       NUMBER(10) DEFAULT 0,
    unit_b_total           NUMBER(10) DEFAULT 0,
    unit_ordinary_effective  NUMBER(10) DEFAULT 0,
    unit_ordinary_total      NUMBER(10) DEFAULT 0,
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- 1.2 门诊运行科室明细表
CREATE TABLE tr_outpatient_operation_detail (
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
    effective_detail NUMBER(10)    DEFAULT 0,
    total_detail    NUMBER(10)      DEFAULT 0,
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- ============================================================
-- 2. 门诊预警统计
-- ============================================================

-- 2.1 门诊预警概览表
CREATE TABLE tr_outpatient_alert_overview (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    remain_alert    NUMBER(10)      DEFAULT 0,          -- 滞留预警
    appointment_alert NUMBER(10)    DEFAULT 0,          -- 预约预警
    early_leave     NUMBER(10)      DEFAULT 0,          -- 早退人数
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- 2.2 门诊预警科室明细表
CREATE TABLE tr_outpatient_alert_dept (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    dept_name       VARCHAR2(100)   NOT NULL,           -- 科室名称
    remain_alert    NUMBER(10)      DEFAULT 0,          -- 滞留预警
    appointment_alert NUMBER(10)    DEFAULT 0,          -- 预约预警
    early_leave     NUMBER(10)      DEFAULT 0,          -- 早退人数
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- 2.3 门诊预警医生明细表
CREATE TABLE tr_outpatient_alert_doctor (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    doctor_name     VARCHAR2(100)   NOT NULL,           -- 医生姓名
    dept_name       VARCHAR2(100),                      -- 科室名称
    remain_alert    NUMBER(10)      DEFAULT 0,          -- 滞留预警
    appointment_alert NUMBER(10)    DEFAULT 0,          -- 预约预警
    early_leave     NUMBER(10)      DEFAULT 0,          -- 早退人数
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- ============================================================
-- 3. 预测门诊量报表
-- ============================================================

-- 3.1 预测门诊量概览表
CREATE TABLE tr_outpatient_forecast_overview (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    tomorrow        NUMBER(10)      DEFAULT 0,          -- 明日预测
    next_week       NUMBER(10)      DEFAULT 0,          -- 下周预测
    next_month      NUMBER(10)      DEFAULT 0,          -- 下月预测
    next_year       NUMBER(10)      DEFAULT 0,          -- 明年预测
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- 3.2 预测门诊量30天明细表
CREATE TABLE tr_outpatient_forecast_month (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    forecast_date   DATE            NOT NULL,           -- 预测日期
    forecast_value  NUMBER(10)      DEFAULT 0,          -- 预测值
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- 3.3 预测门诊量12个月明细表
CREATE TABLE tr_outpatient_forecast_year (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    forecast_month  VARCHAR2(20)    NOT NULL,           -- 预测月份(YYYY-MM)
    forecast_value  NUMBER(10)      DEFAULT 0,          -- 预测值
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- ============================================================
-- 4. 互医质控运营月报
-- ============================================================

-- 4.1 互医质控概览表
CREATE TABLE tr_internet_hospital_overview (
    id              NUMBER(19)      PRIMARY KEY,
    stat_month      VARCHAR2(20)    NOT NULL,           -- 统计月份(YYYY-MM)
    outpatient_volume NUMBER(10)    DEFAULT 0,          -- 门诊量
    doctor_ratio    VARCHAR2(20),                      -- 医师占比
    reception_rate  VARCHAR2(20),                      -- 接诊率
    prescription_rate VARCHAR2(20),                    -- 处方率
    record_rate     VARCHAR2(20),                      -- 病历率
    review_rate     VARCHAR2(20),                      -- 审方率
    execution_rate  VARCHAR2(20),                      -- 执行率
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- 4.2 互医质控运行情况表
CREATE TABLE tr_internet_hospital_operation (
    id              NUMBER(19)      PRIMARY KEY,
    stat_month      VARCHAR2(20)    NOT NULL,           -- 统计月份(YYYY-MM)
    item_name       VARCHAR2(100)   NOT NULL,           -- 指标名称
    current_value   NUMBER(10)      DEFAULT 0,          -- 当月值
    last_value      NUMBER(10)      DEFAULT 0,          -- 上月值
    growth_rate     VARCHAR2(20),                      -- 增长率
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- 4.3 互医质控业务分析图表表
CREATE TABLE tr_internet_hospital_business (
    id              NUMBER(19)      PRIMARY KEY,
    stat_month      VARCHAR2(20)    NOT NULL,           -- 统计月份(YYYY-MM)
    category        VARCHAR2(100)   NOT NULL,           -- 分类
    current_value   NUMBER(10)      DEFAULT 0,          -- 当月值
    last_value      NUMBER(10)      DEFAULT 0,          -- 上月值
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- 4.4 互医质控科室排行表
CREATE TABLE tr_internet_hospital_dept_rank (
    id              NUMBER(19)      PRIMARY KEY,
    stat_month      VARCHAR2(20)    NOT NULL,           -- 统计月份(YYYY-MM)
    rank_num        NUMBER(5)       DEFAULT 0,          -- 排名
    dept_name       VARCHAR2(100)   NOT NULL,           -- 科室名称
    current_month   NUMBER(10)      DEFAULT 0,          -- 当月值
    last_month      NUMBER(10)      DEFAULT 0,          -- 上月值
    growth_rate     VARCHAR2(20),                      -- 增长率
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- 4.5 互医质控医生排行表
CREATE TABLE tr_internet_hospital_doctor_rank (
    id              NUMBER(19)      PRIMARY KEY,
    stat_month      VARCHAR2(20)    NOT NULL,           -- 统计月份(YYYY-MM)
    rank_num        NUMBER(5)       DEFAULT 0,          -- 排名
    doctor_name     VARCHAR2(100)   NOT NULL,           -- 医生姓名
    dept_name       VARCHAR2(100),                      -- 科室名称
    title           VARCHAR2(50),                       -- 职称
    current_month   NUMBER(10)      DEFAULT 0,          -- 当月值
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- 4.6 互医质控增长趋势表
CREATE TABLE tr_internet_hospital_growth (
    id              NUMBER(19)      PRIMARY KEY,
    stat_month      VARCHAR2(20)    NOT NULL,           -- 统计月份(YYYY-MM)
    category        VARCHAR2(100)   NOT NULL,           -- 分类(月份)
    data_value      NUMBER(10)      DEFAULT 0,          -- 数值
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- ============================================================
-- 5. 检验统计
-- ============================================================

-- 5.1 检验统计概览表
CREATE TABLE tr_lab_stats_overview (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    blood_collection NUMBER(10)     DEFAULT 0,          -- 采血人次
    blood_efficiency VARCHAR2(20),                     -- 采血效率
    lab_efficiency  VARCHAR2(20),                      -- 检验效率
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- 5.2 检验统计排行表
CREATE TABLE tr_lab_stats_rank (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    rank_type       VARCHAR2(50)    NOT NULL,           -- 排行类型(BLOOD/LAB)
    rank_num        NUMBER(5)       DEFAULT 0,          -- 排名
    item_name       VARCHAR2(100)   NOT NULL,           -- 项目名称
    item_value      NUMBER(10)      DEFAULT 0,          -- 项目值
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- 5.3 检验统计时段分析表
CREATE TABLE tr_lab_stats_time (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    time_slot       VARCHAR2(50)    NOT NULL,           -- 时段
    blood_count     NUMBER(10)      DEFAULT 0,          -- 采血人次
    lab_count       NUMBER(10)      DEFAULT 0,          -- 检验人次
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- ============================================================
-- 6. 医技统计
-- ============================================================

-- 6.1 医技统计概览表
CREATE TABLE tr_med_tech_overview (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    check_count     NUMBER(10)      DEFAULT 0,          -- 检查人次
    on_time_rate    VARCHAR2(20),                      -- 准时率
    wait_time       VARCHAR2(20),                      -- 等候时长
    avg_wait_late   VARCHAR2(20),                      -- 平均迟到
    avg_report_time VARCHAR2(20),                      -- 平均报告时长
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- 6.2 医技统计科室明细表
CREATE TABLE tr_med_tech_detail (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    dept_name       VARCHAR2(100)   NOT NULL,           -- 科室名称
    check_count     NUMBER(10)      DEFAULT 0,          -- 检查人次
    on_time_rate    VARCHAR2(20),                      -- 准时率
    wait_time       NUMBER(10,2),                       -- 等候时长
    avg_wait_late   NUMBER(10,2),                       -- 平均迟到
    avg_report_time NUMBER(10,2),                       -- 平均报告时长
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- ============================================================
-- 7. 爽约退号分析
-- ============================================================

-- 7.1 爽约退号概览表
CREATE TABLE tr_no_show_overview (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    refund_count    NUMBER(10)      DEFAULT 0,          -- 退号人数
    refund_rate     VARCHAR2(20),                      -- 退号率
    no_show_count   NUMBER(10)      DEFAULT 0,          -- 爽约人数
    no_show_rate    VARCHAR2(20),                      -- 爽约率
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- 7.2 爽约退号科室明细表
CREATE TABLE tr_no_show_detail (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    dept_name       VARCHAR2(100)   NOT NULL,           -- 科室名称
    refund_count    NUMBER(10)      DEFAULT 0,          -- 退号人数
    refund_rate     VARCHAR2(20),                      -- 退号率
    no_show_count   NUMBER(10)      DEFAULT 0,          -- 爽约人数
    no_show_rate    VARCHAR2(20),                      -- 爽约率
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- 7.3 爽约退号来源分析表
CREATE TABLE tr_no_show_origin (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    origin_type     VARCHAR2(50)    NOT NULL,           -- 来源类型(REFUND/NO_SHOW)
    item_name       VARCHAR2(100)   NOT NULL,           -- 来源名称
    item_value      NUMBER(10)      DEFAULT 0,          -- 数量
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- 7.4 爽约退号渠道分析表
CREATE TABLE tr_no_show_channel (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    channel_type    VARCHAR2(50)    NOT NULL,           -- 渠道类型(REFUND/NO_SHOW)
    item_name       VARCHAR2(100)   NOT NULL,           -- 渠道名称
    item_value      NUMBER(10)      DEFAULT 0,          -- 数量
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- 7.5 爽约退号年龄分析表
CREATE TABLE tr_no_show_age (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    age_group       VARCHAR2(50)    NOT NULL,           -- 年龄段
    no_show_count   NUMBER(10)      DEFAULT 0,          -- 爽约人数
    refund_count    NUMBER(10)      DEFAULT 0,          -- 退号人数
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- ============================================================
-- 8. 患者画像
-- ============================================================

-- 8.1 患者画像年龄分析表
CREATE TABLE tr_patient_portrait_age (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    age_group       VARCHAR2(50)    NOT NULL,           -- 年龄段
    archive_count   NUMBER(10)      DEFAULT 0,          -- 建档人数
    outpatient_count NUMBER(10)     DEFAULT 0,          -- 门诊人数
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- 8.2 患者画像医保分析表
CREATE TABLE tr_patient_portrait_insurance (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    insurance_type  VARCHAR2(100)   NOT NULL,           -- 医保类型
    patient_count   NUMBER(10)      DEFAULT 0,          -- 患者人数
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- 8.3 患者画像身份分析表
CREATE TABLE tr_patient_portrait_identity (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    identity_type   VARCHAR2(100)   NOT NULL,           -- 身份类型
    patient_count   NUMBER(10)      DEFAULT 0,          -- 患者人数
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- 8.4 患者画像挂号来源分析表
CREATE TABLE tr_patient_portrait_register (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    source_type     VARCHAR2(100)   NOT NULL,           -- 来源类型
    patient_count   NUMBER(10)      DEFAULT 0,          -- 患者人数
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- 8.5 患者画像建档来源分析表
CREATE TABLE tr_patient_portrait_archive (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    source_type     VARCHAR2(100)   NOT NULL,           -- 来源类型
    patient_count   NUMBER(10)      DEFAULT 0,          -- 患者人数
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- ============================================================
-- 9. 门诊管理质量控制
-- ============================================================

-- 9.1 门诊质量控制概览表
CREATE TABLE tr_quality_control_overview (
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
    create_time         DATE            DEFAULT SYSDATE,
    update_time         DATE            DEFAULT SYSDATE,
    ext1                VARCHAR2(500),
    ext2                VARCHAR2(500),
    ext3                VARCHAR2(500)
);

-- 9.2 门诊质量控制月度明细表
CREATE TABLE tr_quality_control_detail (
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
    create_time         DATE            DEFAULT SYSDATE,
    update_time         DATE            DEFAULT SYSDATE,
    ext1                VARCHAR2(500),
    ext2                VARCHAR2(500),
    ext3                VARCHAR2(500)
);

-- ============================================================
-- 10. 门诊收入分析
-- ============================================================

-- 10.1 门诊收入概览表
CREATE TABLE tr_revenue_overview (
    id                  NUMBER(19)      PRIMARY KEY,
    stat_date           DATE            NOT NULL,       -- 统计日期
    outpatient_revenue  NUMBER(18,2),                   -- 门诊收入
    service_revenue     NUMBER(18,2),                   -- 服务收入
    create_time         DATE            DEFAULT SYSDATE,
    update_time         DATE            DEFAULT SYSDATE,
    ext1                VARCHAR2(500),
    ext2                VARCHAR2(500),
    ext3                VARCHAR2(500)
);

-- 10.2 门诊收入科室明细表
CREATE TABLE tr_revenue_dept (
    id                  NUMBER(19)      PRIMARY KEY,
    stat_date           DATE            NOT NULL,       -- 统计日期
    dept_name           VARCHAR2(100)   NOT NULL,       -- 科室名称
    outpatient_revenue  VARCHAR2(100),                  -- 门诊收入
    service_revenue     VARCHAR2(100),                  -- 服务收入
    create_time         DATE            DEFAULT SYSDATE,
    update_time         DATE            DEFAULT SYSDATE,
    ext1                VARCHAR2(500),
    ext2                VARCHAR2(500),
    ext3                VARCHAR2(500)
);

-- 10.3 门诊收入医生明细表
CREATE TABLE tr_revenue_doctor (
    id                  NUMBER(19)      PRIMARY KEY,
    stat_date           DATE            NOT NULL,       -- 统计日期
    doctor_name         VARCHAR2(100)   NOT NULL,       -- 医生姓名
    dept_name           VARCHAR2(100),                  -- 科室名称
    doctor_benefit      VARCHAR2(100),                  -- 医生收益
    service_revenue     VARCHAR2(100),                  -- 服务收入
    create_time         DATE            DEFAULT SYSDATE,
    update_time         DATE            DEFAULT SYSDATE,
    ext1                VARCHAR2(500),
    ext2                VARCHAR2(500),
    ext3                VARCHAR2(500)
);

-- ============================================================
-- 11. 诊室使用率分析
-- ============================================================

-- 11.1 诊室使用率概览表
CREATE TABLE tr_room_usage_overview (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    avg_usage       VARCHAR2(20),                      -- 平均使用率
    am_usage        VARCHAR2(20),                      -- 上午使用率
    pm_usage        VARCHAR2(20),                      -- 下午使用率
    holiday_usage   VARCHAR2(20),                      -- 节假日使用率
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- 11.2 诊室使用率科室明细表
CREATE TABLE tr_room_usage_detail (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    dept_name       VARCHAR2(100)   NOT NULL,           -- 科室名称
    avg_usage       VARCHAR2(20),                      -- 平均使用率
    am_usage        VARCHAR2(20),                      -- 上午使用率
    pm_usage        VARCHAR2(20),                      -- 下午使用率
    holiday_usage   VARCHAR2(20),                      -- 节假日使用率
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- ============================================================
-- 12. 门诊服务质量分析
-- ============================================================

-- 12.1 门诊服务质量概览表
CREATE TABLE tr_service_quality_overview (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    complaint_count NUMBER(10)      DEFAULT 0,          -- 投诉数量
    praise_count    NUMBER(10)      DEFAULT 0,          -- 表扬数量
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- 12.2 门诊服务质量投诉明细表
CREATE TABLE tr_service_quality_complaint (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    complaint_time  DATE,                               -- 投诉时间
    dept_name       VARCHAR2(100),                      -- 科室
    person_name     VARCHAR2(100),                      -- 人员
    position        VARCHAR2(50),                       -- 职位
    category        VARCHAR2(100),                      -- 分类
    result          VARCHAR2(200),                      -- 处理结果
    remark          VARCHAR2(500),                      -- 备注
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- 12.3 门诊服务质量表扬明细表
CREATE TABLE tr_service_quality_praise (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    praise_time     DATE,                               -- 表扬时间
    dept_name       VARCHAR2(100),                      -- 科室
    person_name     VARCHAR2(100),                      -- 人员
    position        VARCHAR2(50),                       -- 职位
    method          VARCHAR2(100),                      -- 表扬方式
    feedback        VARCHAR2(500),                      -- 反馈内容
    remark          VARCHAR2(500),                      -- 备注
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- ============================================================
-- 13. 专科治疗量统计
-- ============================================================

-- 13.1 专科治疗量概览表
CREATE TABLE tr_specialty_treatment_overview (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    treatment_count NUMBER(10)      DEFAULT 0,          -- 治疗人次
    treatment_amount NUMBER(18,2),                      -- 治疗金额
    patient_count   NUMBER(10)      DEFAULT 0,          -- 患者人数
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- 13.2 专科治疗量科室明细表
CREATE TABLE tr_specialty_treatment_detail (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    dept_name       VARCHAR2(100)   NOT NULL,           -- 科室名称
    treatment_count NUMBER(10)      DEFAULT 0,          -- 治疗人次
    treatment_amount NUMBER(18,2),                      -- 治疗金额
    patient_count   NUMBER(10)      DEFAULT 0,          -- 患者人数
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- ============================================================
-- 14. 人工窗口统计
-- ============================================================

-- 14.1 人工窗口概览表
CREATE TABLE tr_window_stats_overview (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    register_count  NUMBER(10)      DEFAULT 0,          -- 挂号人次
    payment_count   NUMBER(10)      DEFAULT 0,          -- 收费人次
    refund_count    NUMBER(10)      DEFAULT 0,          -- 退费人次
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- 14.2 人工窗口年龄分析表
CREATE TABLE tr_window_stats_age (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    age_group       VARCHAR2(50)    NOT NULL,           -- 年龄段
    patient_count   NUMBER(10)      DEFAULT 0,          -- 人数
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- 14.3 人工窗口时段分析表
CREATE TABLE tr_window_stats_time (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    time_slot       VARCHAR2(50)    NOT NULL,           -- 时段
    business_count  NUMBER(10)      DEFAULT 0,          -- 业务量
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- 14.4 人工窗口来源分析表
CREATE TABLE tr_window_stats_source (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    source_name     VARCHAR2(100)   NOT NULL,           -- 来源名称
    source_count    NUMBER(10)      DEFAULT 0,          -- 数量
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- 14.5 人工窗口工作量表
CREATE TABLE tr_window_stats_workload (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    business_type   VARCHAR2(100)   NOT NULL,           -- 业务类型
    register_count  NUMBER(10)      DEFAULT 0,          -- 挂号数
    payment_count   NUMBER(10)      DEFAULT 0,          -- 收费数
    refund_count    NUMBER(10)      DEFAULT 0,          -- 退费数
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- ============================================================
-- 15. 收费员结账统计
-- ============================================================

-- 15.1 收费员结账概览表
CREATE TABLE tr_cashier_settlement_overview (
    id                      NUMBER(19)      PRIMARY KEY,
    stat_date               DATE            NOT NULL,   -- 统计日期
    appointment_register    NUMBER(10)      DEFAULT 0,  -- 预约挂号
    appointment_register_compare NUMBER(10) DEFAULT 0, -- 对比值
    appointment_fetch     NUMBER(10)      DEFAULT 0,  -- 预约取号
    appointment_fetch_compare NUMBER(10) DEFAULT 0,
    today_register        NUMBER(10)      DEFAULT 0,  -- 当日挂号
    today_register_compare NUMBER(10) DEFAULT 0,
    refund                NUMBER(10)      DEFAULT 0,  -- 退号
    refund_compare        NUMBER(10)      DEFAULT 0,
    outpatient_charge     NUMBER(10)      DEFAULT 0,  -- 门诊收费
    outpatient_charge_compare NUMBER(10) DEFAULT 0,
    outpatient_refund     NUMBER(10)      DEFAULT 0,  -- 门诊退费
    outpatient_refund_compare NUMBER(10) DEFAULT 0,
    prepayment            NUMBER(10)      DEFAULT 0,  -- 预交金
    prepayment_compare    NUMBER(10)      DEFAULT 0,
    hospital_refund       NUMBER(10)      DEFAULT 0,  -- 住院退费
    hospital_refund_compare NUMBER(10) DEFAULT 0,
    discharge_settlement  NUMBER(10)      DEFAULT 0,  -- 出院结算
    discharge_settlement_compare NUMBER(10) DEFAULT 0,
    create_time           DATE            DEFAULT SYSDATE,
    update_time           DATE            DEFAULT SYSDATE,
    ext1                  VARCHAR2(500),
    ext2                  VARCHAR2(500),
    ext3                  VARCHAR2(500)
);

-- 15.2 收费员结账日明细表
CREATE TABLE tr_cashier_settlement_detail (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    item_date       DATE            NOT NULL,           -- 日期
    cashier_name    VARCHAR2(100),                      -- 收费员
    item_type       VARCHAR2(50)    NOT NULL,           -- 项目类型
    item_value      NUMBER(18,2),                       -- 金额
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- 15.3 收费员结账图表数据表
CREATE TABLE tr_cashier_settlement_chart (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    chart_title     VARCHAR2(200),                      -- 图表标题
    chart_subtitle  VARCHAR2(200),                      -- 副标题
    date_range      VARCHAR2(100),                      -- 日期范围
    category        VARCHAR2(100)   NOT NULL,           -- 分类
    data_value      NUMBER(10)      DEFAULT 0,          -- 数值
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- ============================================================
-- 16. 出院结算报表
-- ============================================================

-- 16.1 出院结算概览表
CREATE TABLE tr_discharge_settlement_overview (
    id                      NUMBER(19)      PRIMARY KEY,
    stat_date               DATE            NOT NULL,   -- 统计日期
    total_discharge_count   NUMBER(10)      DEFAULT 0,  -- 总出院人次
    total_discharge_compare NUMBER(10)      DEFAULT 0,
    discharged_count        NUMBER(10)      DEFAULT 0,  -- 已出院人次
    discharged_compare      NUMBER(10)      DEFAULT 0,
    not_discharged_count    NUMBER(10)      DEFAULT 0,  -- 未出院人次
    not_discharged_compare  NUMBER(10)      DEFAULT 0,
    settlement_amount       NUMBER(18,2),               -- 结算金额
    settlement_amount_compare NUMBER(10)    DEFAULT 0,
    create_time             DATE            DEFAULT SYSDATE,
    update_time             DATE            DEFAULT SYSDATE,
    ext1                    VARCHAR2(500),
    ext2                    VARCHAR2(500),
    ext3                    VARCHAR2(500)
);

-- 16.2 出院结算日明细表
CREATE TABLE tr_discharge_settlement_detail (
    id                      NUMBER(19)      PRIMARY KEY,
    stat_date               DATE            NOT NULL,   -- 统计日期
    item_date               DATE            NOT NULL,   -- 日期
    total_last              NUMBER(10)      DEFAULT 0,  -- 总出院上期
    total_current           NUMBER(10)      DEFAULT 0,  -- 总出院本期
    total_compare           NUMBER(10)      DEFAULT 0,
    discharged_last         NUMBER(10)      DEFAULT 0,  -- 已出院上期
    discharged_current      NUMBER(10)      DEFAULT 0,  -- 已出院本期
    discharged_compare      NUMBER(10)      DEFAULT 0,
    not_discharged_last     NUMBER(10)      DEFAULT 0,  -- 未出院上期
    not_discharged_current  NUMBER(10)      DEFAULT 0,  -- 未出院本期
    not_discharged_compare  NUMBER(10)      DEFAULT 0,
    amount_last             NUMBER(18,2),               -- 金额上期
    amount_current          NUMBER(18,2),               -- 金额本期
    amount_compare          NUMBER(10)      DEFAULT 0,
    create_time             DATE            DEFAULT SYSDATE,
    update_time             DATE            DEFAULT SYSDATE,
    ext1                    VARCHAR2(500),
    ext2                    VARCHAR2(500),
    ext3                    VARCHAR2(500)
);

-- 16.3 出院结算图表分析表
CREATE TABLE tr_discharge_settlement_chart (
    id              NUMBER(19)      PRIMARY KEY,
    stat_date       DATE            NOT NULL,           -- 统计日期
    chart_type      VARCHAR2(50)    NOT NULL,           -- 图表类型(CHANNEL/PATIENT_TYPE/AMOUNT_TYPE)
    item_name       VARCHAR2(100)   NOT NULL,           -- 项目名称
    item_value      NUMBER(10)      DEFAULT 0,          -- 数值
    item_compare    NUMBER(10)      DEFAULT 0,          -- 对比值
    create_time     DATE            DEFAULT SYSDATE,
    update_time     DATE            DEFAULT SYSDATE,
    ext1            VARCHAR2(500),
    ext2            VARCHAR2(500),
    ext3            VARCHAR2(500)
);

-- ============================================================
-- 创建序列（Oracle 主键自增）
-- ============================================================

CREATE SEQUENCE seq_tr_reports START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 创建索引
-- ============================================================

-- 按统计日期索引
CREATE INDEX idx_tr_op_overview_date ON tr_outpatient_operation_overview(stat_date);
CREATE INDEX idx_tr_op_detail_date ON tr_outpatient_operation_detail(stat_date);
CREATE INDEX idx_tr_alert_overview_date ON tr_outpatient_alert_overview(stat_date);
CREATE INDEX idx_tr_alert_dept_date ON tr_outpatient_alert_dept(stat_date);
CREATE INDEX idx_tr_alert_doctor_date ON tr_outpatient_alert_doctor(stat_date);
CREATE INDEX idx_tr_forecast_overview_date ON tr_outpatient_forecast_overview(stat_date);
CREATE INDEX idx_tr_internet_overview_month ON tr_internet_hospital_overview(stat_month);
CREATE INDEX idx_tr_lab_overview_date ON tr_lab_stats_overview(stat_date);
CREATE INDEX idx_tr_med_overview_date ON tr_med_tech_overview(stat_date);
CREATE INDEX idx_tr_no_show_overview_date ON tr_no_show_overview(stat_date);
CREATE INDEX idx_tr_portrait_age_date ON tr_patient_portrait_age(stat_date);
CREATE INDEX idx_tr_qc_overview_date ON tr_quality_control_overview(stat_date);
CREATE INDEX idx_tr_revenue_overview_date ON tr_revenue_overview(stat_date);
CREATE INDEX idx_tr_room_overview_date ON tr_room_usage_overview(stat_date);
CREATE INDEX idx_tr_service_overview_date ON tr_service_quality_overview(stat_date);
CREATE INDEX idx_tr_specialty_overview_date ON tr_specialty_treatment_overview(stat_date);
CREATE INDEX idx_tr_window_overview_date ON tr_window_stats_overview(stat_date);
CREATE INDEX idx_tr_cashier_overview_date ON tr_cashier_settlement_overview(stat_date);
CREATE INDEX idx_tr_discharge_overview_date ON tr_discharge_settlement_overview(stat_date);

-- 按科室名称索引
CREATE INDEX idx_tr_op_detail_dept ON tr_outpatient_operation_detail(dept_name);
CREATE INDEX idx_tr_alert_dept_name ON tr_outpatient_alert_dept(dept_name);
CREATE INDEX idx_tr_alert_doctor_name ON tr_outpatient_alert_doctor(doctor_name);
CREATE INDEX idx_tr_med_detail_dept ON tr_med_tech_detail(dept_name);
CREATE INDEX idx_tr_no_show_detail_dept ON tr_no_show_detail(dept_name);
CREATE INDEX idx_tr_revenue_dept_name ON tr_revenue_dept(dept_name);
CREATE INDEX idx_tr_revenue_doctor_name ON tr_revenue_doctor(doctor_name);
CREATE INDEX idx_tr_room_detail_dept ON tr_room_usage_detail(dept_name);
CREATE INDEX idx_tr_specialty_detail_dept ON tr_specialty_treatment_detail(dept_name);

-- ============================================================
-- 添加表注释
-- ============================================================

COMMENT ON TABLE tr_outpatient_operation_overview IS '门诊运行数据统计-概览';
COMMENT ON TABLE tr_outpatient_operation_detail IS '门诊运行数据统计-科室明细';
COMMENT ON TABLE tr_outpatient_alert_overview IS '门诊预警统计-概览';
COMMENT ON TABLE tr_outpatient_alert_dept IS '门诊预警统计-科室明细';
COMMENT ON TABLE tr_outpatient_alert_doctor IS '门诊预警统计-医生明细';
COMMENT ON TABLE tr_outpatient_forecast_overview IS '预测门诊量报表-概览';
COMMENT ON TABLE tr_outpatient_forecast_month IS '预测门诊量报表-30天明细';
COMMENT ON TABLE tr_outpatient_forecast_year IS '预测门诊量报表-12个月明细';
COMMENT ON TABLE tr_internet_hospital_overview IS '互医质控运营月报-概览';
COMMENT ON TABLE tr_internet_hospital_operation IS '互医质控运营月报-运行情况';
COMMENT ON TABLE tr_internet_hospital_business IS '互医质控运营月报-业务分析';
COMMENT ON TABLE tr_internet_hospital_dept_rank IS '互医质控运营月报-科室排行';
COMMENT ON TABLE tr_internet_hospital_doctor_rank IS '互医质控运营月报-医生排行';
COMMENT ON TABLE tr_internet_hospital_growth IS '互医质控运营月报-增长趋势';
COMMENT ON TABLE tr_lab_stats_overview IS '检验统计-概览';
COMMENT ON TABLE tr_lab_stats_rank IS '检验统计-排行';
COMMENT ON TABLE tr_lab_stats_time IS '检验统计-时段分析';
COMMENT ON TABLE tr_med_tech_overview IS '医技统计-概览';
COMMENT ON TABLE tr_med_tech_detail IS '医技统计-科室明细';
COMMENT ON TABLE tr_no_show_overview IS '爽约退号分析-概览';
COMMENT ON TABLE tr_no_show_detail IS '爽约退号分析-科室明细';
COMMENT ON TABLE tr_no_show_origin IS '爽约退号分析-来源分析';
COMMENT ON TABLE tr_no_show_channel IS '爽约退号分析-渠道分析';
COMMENT ON TABLE tr_no_show_age IS '爽约退号分析-年龄分析';
COMMENT ON TABLE tr_patient_portrait_age IS '患者画像-年龄分析';
COMMENT ON TABLE tr_patient_portrait_insurance IS '患者画像-医保分析';
COMMENT ON TABLE tr_patient_portrait_identity IS '患者画像-身份分析';
COMMENT ON TABLE tr_patient_portrait_register IS '患者画像-挂号来源';
COMMENT ON TABLE tr_patient_portrait_archive IS '患者画像-建档来源';
COMMENT ON TABLE tr_quality_control_overview IS '门诊管理质量控制-概览';
COMMENT ON TABLE tr_quality_control_detail IS '门诊管理质量控制-月度明细';
COMMENT ON TABLE tr_revenue_overview IS '门诊收入分析-概览';
COMMENT ON TABLE tr_revenue_dept IS '门诊收入分析-科室明细';
COMMENT ON TABLE tr_revenue_doctor IS '门诊收入分析-医生明细';
COMMENT ON TABLE tr_room_usage_overview IS '诊室使用率分析-概览';
COMMENT ON TABLE tr_room_usage_detail IS '诊室使用率分析-科室明细';
COMMENT ON TABLE tr_service_quality_overview IS '门诊服务质量分析-概览';
COMMENT ON TABLE tr_service_quality_complaint IS '门诊服务质量分析-投诉明细';
COMMENT ON TABLE tr_service_quality_praise IS '门诊服务质量分析-表扬明细';
COMMENT ON TABLE tr_specialty_treatment_overview IS '专科治疗量统计-概览';
COMMENT ON TABLE tr_specialty_treatment_detail IS '专科治疗量统计-科室明细';
COMMENT ON TABLE tr_window_stats_overview IS '人工窗口统计-概览';
COMMENT ON TABLE tr_window_stats_age IS '人工窗口统计-年龄分析';
COMMENT ON TABLE tr_window_stats_time IS '人工窗口统计-时段分析';
COMMENT ON TABLE tr_window_stats_source IS '人工窗口统计-来源分析';
COMMENT ON TABLE tr_window_stats_workload IS '人工窗口统计-工作量';
COMMENT ON TABLE tr_cashier_settlement_overview IS '收费员结账统计-概览';
COMMENT ON TABLE tr_cashier_settlement_detail IS '收费员结账统计-日明细';
COMMENT ON TABLE tr_cashier_settlement_chart IS '收费员结账统计-图表';
COMMENT ON TABLE tr_discharge_settlement_overview IS '出院结算报表-概览';
COMMENT ON TABLE tr_discharge_settlement_detail IS '出院结算报表-日明细';
COMMENT ON TABLE tr_discharge_settlement_chart IS '出院结算报表-图表';

-- ============================================================
-- 添加表注释
-- ============================================================

COMMENT ON TABLE tr_outpatient_operation_overview IS '门诊运行数据统计-概览（存储每日门诊运行总览指标：就诊人次、预约率、检查率、效率、单元数等）';
COMMENT ON TABLE tr_outpatient_operation_detail IS '门诊运行数据统计-科室明细（按科室维度存储就诊人次、预约率、检查率、效率及按职称分类的就诊人次明细）';
COMMENT ON TABLE tr_outpatient_alert_overview IS '门诊预警统计-概览（存储每日门诊预警总览：滞留预警、预约预警、早退人数）';
COMMENT ON TABLE tr_outpatient_alert_dept IS '门诊预警统计-科室明细（按科室维度统计滞留预警、预约预警、早退人数）';
COMMENT ON TABLE tr_outpatient_alert_doctor IS '门诊预警统计-医生明细（按医生维度统计滞留预警、预约预警、早退人数）';
COMMENT ON TABLE tr_outpatient_forecast_overview IS '预测门诊量报表-概览（存储门诊量预测总览：明日、下周、下月、明年的预测值）';
COMMENT ON TABLE tr_outpatient_forecast_month IS '预测门诊量报表-30天明细（存储未来30天每日门诊量预测值）';
COMMENT ON TABLE tr_outpatient_forecast_year IS '预测门诊量报表-12个月明细（存储未来12个月每月门诊量预测值）';
COMMENT ON TABLE tr_internet_hospital_overview IS '互医质控运营月报-概览（存储互联网医院月度质控总览指标：门诊量、接诊率、处方率、审方率等）';
COMMENT ON TABLE tr_internet_hospital_operation IS '互医质控运营月报-运行情况（互联网医院各运营指标的运行情况对比：当月 vs 上月）';
COMMENT ON TABLE tr_internet_hospital_business IS '互医质控运营月报-业务分析（互联网医院业务分析图表数据，按分类存储当月/上月对比）';
COMMENT ON TABLE tr_internet_hospital_dept_rank IS '互医质控运营月报-科室排行（互联网医院按科室的门诊量排行数据）';
COMMENT ON TABLE tr_internet_hospital_doctor_rank IS '互医质控运营月报-医生排行（互联网医院按医生的门诊量排行数据）';
COMMENT ON TABLE tr_internet_hospital_growth IS '互医质控运营月报-增长趋势（互联网医院各月份的增长趋势数据，用于绘制趋势图）';
COMMENT ON TABLE tr_lab_stats_overview IS '检验统计-概览（检验科运营总览指标：采血人次、采血效率、检验效率）';
COMMENT ON TABLE tr_lab_stats_rank IS '检验统计-排行（检验项目或科室的排行数据：采血排行/检验排行）';
COMMENT ON TABLE tr_lab_stats_time IS '检验统计-时段分析（检验科各时段的业务量分布）';
COMMENT ON TABLE tr_med_tech_overview IS '医技统计-概览（医技科室运营总览指标：检查人次、准时率、等候时长、报告时长）';
COMMENT ON TABLE tr_med_tech_detail IS '医技统计-科室明细（按医技科室维度统计检查人次、准时率、等候时长等）';
COMMENT ON TABLE tr_no_show_overview IS '爽约退号分析-概览（爽约和退号情况的总览指标）';
COMMENT ON TABLE tr_no_show_detail IS '爽约退号分析-科室明细（按科室维度统计爽约退号数据）';
COMMENT ON TABLE tr_no_show_origin IS '爽约退号分析-来源分析（按挂号来源维度分析爽约/退号分布）';
COMMENT ON TABLE tr_no_show_channel IS '爽约退号分析-渠道分析（按渠道维度分析爽约/退号分布）';
COMMENT ON TABLE tr_no_show_age IS '爽约退号分析-年龄分析（按年龄段分析爽约/退号分布）';
COMMENT ON TABLE tr_patient_portrait_age IS '患者画像-年龄分析（按年龄段分析患者建档和门诊就诊分布）';
COMMENT ON TABLE tr_patient_portrait_insurance IS '患者画像-医保分析（按医保类型分析患者分布）';
COMMENT ON TABLE tr_patient_portrait_identity IS '患者画像-身份分析（按患者身份类型分析分布）';
COMMENT ON TABLE tr_patient_portrait_register IS '患者画像-挂号来源（按挂号来源分析患者分布）';
COMMENT ON TABLE tr_patient_portrait_archive IS '患者画像-建档来源（按建档来源分析患者分布）';
COMMENT ON TABLE tr_quality_control_overview IS '门诊管理质量控制-概览（门诊质量管理各项质控指标的总览数据）';
COMMENT ON TABLE tr_quality_control_detail IS '门诊管理质量控制-月度明细（门诊质控指标按月度的详细记录，用于趋势分析）';
COMMENT ON TABLE tr_revenue_overview IS '门诊收入分析-概览（门诊收入的总览数据：门诊总收入和服务收入）';
COMMENT ON TABLE tr_revenue_dept IS '门诊收入分析-科室明细（按科室维度统计门诊收入和服务收入）';
COMMENT ON TABLE tr_revenue_doctor IS '门诊收入分析-医生明细（按医生维度统计门诊收益和服务收入）';
COMMENT ON TABLE tr_room_usage_overview IS '诊室使用率分析-概览（诊室使用率的总览指标：平均使用率、上午/下午/节假日使用率）';
COMMENT ON TABLE tr_room_usage_detail IS '诊室使用率分析-科室明细（按科室维度统计诊室使用率）';
COMMENT ON TABLE tr_service_quality_overview IS '门诊服务质量分析-概览（门诊服务质量总览：投诉数量和表扬数量）';
COMMENT ON TABLE tr_service_quality_complaint IS '门诊服务质量分析-投诉明细（门诊投诉事件的详细记录）';
COMMENT ON TABLE tr_service_quality_praise IS '门诊服务质量分析-表扬明细（门诊表扬事件的详细记录）';
COMMENT ON TABLE tr_specialty_treatment_overview IS '专科治疗量统计-概览（专科治疗量的总览数据：治疗人次、治疗金额、患者人数）';
COMMENT ON TABLE tr_specialty_treatment_detail IS '专科治疗量统计-科室明细（按科室维度统计专科治疗量）';
COMMENT ON TABLE tr_window_stats_overview IS '人工窗口统计-概览（人工窗口业务量的总览指标：挂号/收费/退费人次）';
COMMENT ON TABLE tr_window_stats_age IS '人工窗口统计-年龄分析（按年龄段分析窗口业务分布）';
COMMENT ON TABLE tr_window_stats_time IS '人工窗口统计-时段分析（按时段分析窗口业务量分布）';
COMMENT ON TABLE tr_window_stats_source IS '人工窗口统计-来源分析（按患者来源分析窗口业务分布）';
COMMENT ON TABLE tr_window_stats_workload IS '人工窗口统计-工作量（按业务类型统计窗口工作量：挂号/收费/退费）';
COMMENT ON TABLE tr_cashier_settlement_overview IS '收费员结账统计-概览（收费员结账业务的总览指标：各类业务笔数及对比值）';
COMMENT ON TABLE tr_cashier_settlement_detail IS '收费员结账统计-日明细（收费员每日结账的明细记录）';
COMMENT ON TABLE tr_cashier_settlement_chart IS '收费员结账统计-图表（收费员结账图表所需的结构化数据）';
COMMENT ON TABLE tr_discharge_settlement_overview IS '出院结算报表-概览（出院结算业务的总览指标：出院人次、结算金额）';
COMMENT ON TABLE tr_discharge_settlement_detail IS '出院结算报表-日明细（出院结算按日期的详细对比数据：本期 vs 上期）';
COMMENT ON TABLE tr_discharge_settlement_chart IS '出院结算报表-图表（出院结算图表数据：按图表类型和项目分类存储）';

-- ============================================================
-- SQL 文件结束
-- ============================================================
