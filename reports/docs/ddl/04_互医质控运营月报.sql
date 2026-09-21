-- ============================================================
-- 互医质控运营月报
-- ============================================================

-- 清理已存在对象(如重建请先执行)

DROP TABLE tr_inet_hosp_ov CASCADE CONSTRAINTS;

DROP TABLE tr_inet_hosp_op CASCADE CONSTRAINTS;

DROP TABLE tr_inet_hosp_biz CASCADE CONSTRAINTS;

DROP TABLE tr_inet_hosp_dept_rnk CASCADE CONSTRAINTS;

DROP TABLE tr_inet_hosp_doc_rnk CASCADE CONSTRAINTS;

DROP TABLE tr_inet_hosp_grw CASCADE CONSTRAINTS;

-- 4.1 互医质控概览表
CREATE TABLE tr_inet_hosp_ov (
    stat_date       DATE            NOT NULL,           -- 统计日期(YYYY-MM-DD)
    outpatient_volume NUMBER(10)    DEFAULT 0,          -- 门诊量
    doctor_ratio_num    NUMBER(18,4),                  -- 互联网医师占比-分子
    doctor_ratio_den    NUMBER(18,4),                  -- 互联网医师占比-分母
    reception_rate_num  NUMBER(18,4),                  -- 互联网医院接诊率-分子
    reception_rate_den  NUMBER(18,4),                  -- 互联网医院接诊率-分母
    prescription_rate_num NUMBER(18,4),                -- 互联网医院处方开具率-分子
    prescription_rate_den NUMBER(18,4),                -- 互联网医院处方开具率-分母
    record_rate_num     NUMBER(18,4),                  -- 互联网医院病历书写率-分子
    record_rate_den     NUMBER(18,4),                  -- 互联网医院病历书写率-分母
    review_rate_num     NUMBER(18,4),                  -- 互联网医院处方点评率-分子
    review_rate_den     NUMBER(18,4),                  -- 互联网医院处方点评率-分母
    execution_rate_num  NUMBER(18,4),                  -- 互联网医院药品处方执行率-分子
    execution_rate_den  NUMBER(18,4),                  -- 互联网医院药品处方执行率-分母
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1(数据来源: 人工登记/接口同步)
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 4.2 互医质控运行情况表
CREATE TABLE tr_inet_hosp_op (
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

-- 4.6 互医质控平均候诊时长表（科室TOP20）
CREATE TABLE tr_inet_hosp_grw (
    stat_month      VARCHAR2(20)    NOT NULL,           -- 统计月份(YYYY-MM)
    category        VARCHAR2(100)   NOT NULL,           -- 分类(科室名称)
    data_value      NUMBER(10)      DEFAULT 0,          -- 数值(平均候诊时长,分钟)
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 创建索引

CREATE INDEX idx_tr_inet_hosp_ov_date ON tr_inet_hosp_ov(stat_date);

-- 添加注释

COMMENT ON TABLE tr_inet_hosp_ov IS '互医质控运营月报-概览';
COMMENT ON TABLE tr_inet_hosp_ov IS '互医质控运营月报-概览(存储互联网医院月度质控总览指标:门诊量、接诊率、处方率、审方率等)';
COMMENT ON COLUMN tr_inet_hosp_ov.stat_date IS '统计日期(YYYY-MM-DD)，按月查询时取该月日期范围聚合';
COMMENT ON COLUMN tr_inet_hosp_ov.outpatient_volume IS '门诊量';
COMMENT ON COLUMN tr_inet_hosp_ov.doctor_ratio_num IS '互联网医师占比-分子';
COMMENT ON COLUMN tr_inet_hosp_ov.doctor_ratio_den IS '互联网医师占比-分母';
COMMENT ON COLUMN tr_inet_hosp_ov.reception_rate_num IS '互联网医院接诊率-分子';
COMMENT ON COLUMN tr_inet_hosp_ov.reception_rate_den IS '互联网医院接诊率-分母';
COMMENT ON COLUMN tr_inet_hosp_ov.prescription_rate_num IS '互联网医院处方开具率-分子';
COMMENT ON COLUMN tr_inet_hosp_ov.prescription_rate_den IS '互联网医院处方开具率-分母';
COMMENT ON COLUMN tr_inet_hosp_ov.record_rate_num IS '互联网医院病历书写率-分子';
COMMENT ON COLUMN tr_inet_hosp_ov.record_rate_den IS '互联网医院病历书写率-分母';
COMMENT ON COLUMN tr_inet_hosp_ov.review_rate_num IS '互联网医院处方点评率-分子';
COMMENT ON COLUMN tr_inet_hosp_ov.review_rate_den IS '互联网医院处方点评率-分母';
COMMENT ON COLUMN tr_inet_hosp_ov.execution_rate_num IS '互联网医院药品处方执行率-分子';
COMMENT ON COLUMN tr_inet_hosp_ov.execution_rate_den IS '互联网医院药品处方执行率-分母';
COMMENT ON COLUMN tr_inet_hosp_ov.create_time IS '创建时间';
COMMENT ON COLUMN tr_inet_hosp_ov.update_time IS '更新时间';
COMMENT ON COLUMN tr_inet_hosp_ov.ext1 IS '扩展字段1(数据来源: 人工登记/接口同步)';
COMMENT ON COLUMN tr_inet_hosp_ov.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_inet_hosp_ov.ext3 IS '扩展字段3';
COMMENT ON TABLE tr_inet_hosp_op IS '互医质控运营月报-运行情况';
COMMENT ON TABLE tr_inet_hosp_op IS '互医质控运营月报-运行情况(互联网医院各运营指标的运行情况对比:当月 vs 上月)';
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
COMMENT ON TABLE tr_inet_hosp_biz IS '互医质控运营月报-业务分析';
COMMENT ON TABLE tr_inet_hosp_biz IS '互医质控运营月报-业务分析(互联网医院业务分析图表数据,按分类存储当月/上月对比)';
COMMENT ON COLUMN tr_inet_hosp_biz.stat_month IS '统计月份(YYYY-MM)';
COMMENT ON COLUMN tr_inet_hosp_biz.category IS '分类';
COMMENT ON COLUMN tr_inet_hosp_biz.current_value IS '当月值';
COMMENT ON COLUMN tr_inet_hosp_biz.last_value IS '上月值';
COMMENT ON COLUMN tr_inet_hosp_biz.create_time IS '创建时间';
COMMENT ON COLUMN tr_inet_hosp_biz.update_time IS '更新时间';
COMMENT ON COLUMN tr_inet_hosp_biz.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_inet_hosp_biz.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_inet_hosp_biz.ext3 IS '扩展字段3';
COMMENT ON TABLE tr_inet_hosp_dept_rnk IS '互医质控运营月报-科室排行';
COMMENT ON TABLE tr_inet_hosp_dept_rnk IS '互医质控运营月报-科室排行(互联网医院按科室的门诊量排行数据)';
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
COMMENT ON TABLE tr_inet_hosp_doc_rnk IS '互医质控运营月报-医生排行';
COMMENT ON TABLE tr_inet_hosp_doc_rnk IS '互医质控运营月报-医生排行(互联网医院按医生的门诊量排行数据)';
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
COMMENT ON TABLE tr_inet_hosp_grw IS '互医质控运营月报-增长趋势';
COMMENT ON TABLE tr_inet_hosp_grw IS '互医质控运营月报-增长趋势(互联网医院各月份的增长趋势数据,用于绘制趋势图)';
COMMENT ON COLUMN tr_inet_hosp_grw.stat_month IS '统计月份(YYYY-MM)';
COMMENT ON COLUMN tr_inet_hosp_grw.category IS '分类(月份)';
COMMENT ON COLUMN tr_inet_hosp_grw.data_value IS '数值';
COMMENT ON COLUMN tr_inet_hosp_grw.create_time IS '创建时间';
COMMENT ON COLUMN tr_inet_hosp_grw.update_time IS '更新时间';
COMMENT ON COLUMN tr_inet_hosp_grw.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_inet_hosp_grw.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_inet_hosp_grw.ext3 IS '扩展字段3';
