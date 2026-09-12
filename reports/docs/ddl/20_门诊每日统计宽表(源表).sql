-- ============================================================
-- 门诊每日统计宽表(HIS每日抽取,爽约退号分析/预测门诊量共用)
-- ============================================================

-- 清理已存在对象(如重建请先执行)

DROP TABLE TR_OUTPATIENT_STATS_DAY_RESULT CASCADE CONSTRAINTS;

CREATE TABLE TR_OUTPATIENT_STATS_DAY_RESULT (
    id                    NUMBER(19)      PRIMARY KEY,
    stats_date            DATE            NOT NULL,   -- 统计日期
    dept_code             VARCHAR2(50),               -- 科室编码
    total_guahao          NUMBER(10)      DEFAULT 0,  -- 总挂号数
    tui_hao_shu           NUMBER(10)      DEFAULT 0,  -- 退号数
    shuang_yue_shu        NUMBER(10)      DEFAULT 0,  -- 爽约数
    tui_hao_chong_qing    NUMBER(10)      DEFAULT 0,  -- 退号-重庆
    tui_hao_si_chuan      NUMBER(10)      DEFAULT 0,  -- 退号-四川
    tui_hao_gui_zhou      NUMBER(10)      DEFAULT 0,  -- 退号-贵州
    tui_hao_yun_nan       NUMBER(10)      DEFAULT 0,  -- 退号-云南
    tui_hao_qi_ta         NUMBER(10)      DEFAULT 0,  -- 退号-其他
    shuang_yue_chong_qing NUMBER(10)      DEFAULT 0,  -- 爽约-重庆
    shuang_yue_si_chuan   NUMBER(10)      DEFAULT 0,  -- 爽约-四川
    shuang_yue_gui_zhou   NUMBER(10)      DEFAULT 0,  -- 爽约-贵州
    shuang_yue_yun_nan    NUMBER(10)      DEFAULT 0,  -- 爽约-云南
    shuang_yue_qi_ta      NUMBER(10)      DEFAULT 0,  -- 爽约-其他
    tui_hao_chuang_kou    NUMBER(10)      DEFAULT 0,  -- 退号-窗口
    tui_hao_zi_zhu_ji     NUMBER(10)      DEFAULT 0,  -- 退号-自助机
    tui_hao_age_0_14        NUMBER(10)      DEFAULT 0,          -- 退号年龄0-14岁
    tui_hao_age_15_19        NUMBER(10)      DEFAULT 0,          -- 退号年龄15-19岁
    tui_hao_age_20_29        NUMBER(10)      DEFAULT 0,          -- 退号年龄20-29岁
    tui_hao_age_30_39        NUMBER(10)      DEFAULT 0,          -- 退号年龄30-39岁
    tui_hao_age_40_49        NUMBER(10)      DEFAULT 0,          -- 退号年龄40-49岁
    tui_hao_age_50_59        NUMBER(10)      DEFAULT 0,          -- 退号年龄50-59岁
    tui_hao_age_60_69        NUMBER(10)      DEFAULT 0,          -- 退号年龄60-69岁
    tui_hao_age_70_79        NUMBER(10)      DEFAULT 0,          -- 退号年龄70-79岁
    tui_hao_age_80_89        NUMBER(10)      DEFAULT 0,          -- 退号年龄80-89岁
    tui_hao_age_90_up        NUMBER(10)      DEFAULT 0,          -- 退号年龄90-up岁,
    shuang_yue_age_0_14     NUMBER(10)      DEFAULT 0,          -- 爽约年龄0-14岁
    shuang_yue_age_15_19     NUMBER(10)      DEFAULT 0,          -- 爽约年龄15-19岁
    shuang_yue_age_20_29     NUMBER(10)      DEFAULT 0,          -- 爽约年龄20-29岁
    shuang_yue_age_30_39     NUMBER(10)      DEFAULT 0,          -- 爽约年龄30-39岁
    shuang_yue_age_40_49     NUMBER(10)      DEFAULT 0,          -- 爽约年龄40-49岁
    shuang_yue_age_50_59     NUMBER(10)      DEFAULT 0,          -- 爽约年龄50-59岁
    shuang_yue_age_60_69     NUMBER(10)      DEFAULT 0,          -- 爽约年龄60-69岁
    shuang_yue_age_70_79     NUMBER(10)      DEFAULT 0,          -- 爽约年龄70-79岁
    shuang_yue_age_80_89     NUMBER(10)      DEFAULT 0,          -- 爽约年龄80-89岁
    shuang_yue_age_90_up     NUMBER(10)      DEFAULT 0           -- 爽约年龄90-up岁
);
CREATE INDEX idx_tr_stats_day_date ON TR_OUTPATIENT_STATS_DAY_RESULT(stats_date);
CREATE INDEX idx_tr_stats_day_dept ON TR_OUTPATIENT_STATS_DAY_RESULT(dept_code);

COMMENT ON TABLE TR_OUTPATIENT_STATS_DAY_RESULT IS '门诊每日统计宽表(HIS每日抽取,爽约退号分析/预测门诊量共用)';
