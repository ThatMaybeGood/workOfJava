-- ============================================================
-- 医保对账每日对比
-- ============================================================
-- 医保侧: 界面上传医保对账xls(4列: 定点医药机构编号/对账数据量/医疗费总额/结算日期),
-- 按结算日期聚合落明细表tr_mi_recon_detail(类型+结算日期唯一, 一天一行), 上传记录落tr_mi_recon_file;
-- 同类型同结算日期以最新上传为准(先删后插), 类型可修改并级联明细
-- 医疗机构侧: 直查HIS(占坑, SQL待补全), 与医保侧同类型同结算日期金额对比, 不等标红
-- 医保类型: 310职工 390居民 3101异地职工 3901异地居民

DROP TABLE tr_mi_recon_detail CASCADE CONSTRAINTS;
DROP TABLE tr_mi_recon_file CASCADE CONSTRAINTS;

CREATE TABLE tr_mi_recon_file (
    id               NUMBER(10)      NOT NULL,               -- 主键(共用seq_tr_reports)
    file_name        VARCHAR2(200)   NOT NULL,               -- 原始文件名
    insur_type       VARCHAR2(4)     NOT NULL,               -- 医保类型: 310职工 390居民 3101异地职工 3901异地居民
    insur_type_name  VARCHAR2(20)    NOT NULL,               -- 医保类型中文名(职工/居民/异地职工/异地居民, 保存时落库)
    data_date        DATE            NOT NULL,               -- 数据日期(上传时选择, 默认当天)
    row_count        NUMBER(6),                              -- 明细行数(按结算日期聚合后)
    create_time      DATE            DEFAULT SYSDATE,
    update_time      DATE            DEFAULT SYSDATE,
    CONSTRAINT pk_tr_mi_recon_file PRIMARY KEY (id)
);

CREATE TABLE tr_mi_recon_detail (
    id               NUMBER(10)      NOT NULL,               -- 主键(共用seq_tr_reports)
    file_id          NUMBER(10)      NOT NULL,               -- 来源上传记录tr_mi_recon_file.id
    insur_type       VARCHAR2(4)     NOT NULL,               -- 医保类型(与上传记录一致, 类型修改级联)
    insur_type_name  VARCHAR2(20)    NOT NULL,               -- 医保类型中文名(保存时落库, 类型修改级联)
    qty              NUMBER(10),                             -- 对账数据量(同日多行已聚合)
    total_fee        NUMBER(14,2),                           -- 医疗费总额(同日多行已聚合)
    settle_date      DATE            NOT NULL,               -- 结算日期
    create_time      DATE            DEFAULT SYSDATE,
    CONSTRAINT pk_tr_mi_recon_detail PRIMARY KEY (id),
    CONSTRAINT uk_tr_mi_recon_detail_td UNIQUE (insur_type, settle_date)
);

COMMENT ON TABLE tr_mi_recon_file IS '医保对账每日对比-上传记录表';
COMMENT ON COLUMN tr_mi_recon_file.id IS '主键(共用seq_tr_reports)';
COMMENT ON COLUMN tr_mi_recon_file.file_name IS '原始文件名';
COMMENT ON COLUMN tr_mi_recon_file.insur_type IS '医保类型: 310职工 390居民 3101异地职工 3901异地居民';
COMMENT ON COLUMN tr_mi_recon_file.insur_type_name IS '医保类型中文名(职工/居民/异地职工/异地居民, 保存时落库)';
COMMENT ON COLUMN tr_mi_recon_file.data_date IS '数据日期(上传时选择, 默认当天)';
COMMENT ON COLUMN tr_mi_recon_file.row_count IS '明细行数(按结算日期聚合后)';
COMMENT ON COLUMN tr_mi_recon_file.create_time IS '上传时间';
COMMENT ON COLUMN tr_mi_recon_file.update_time IS '更新时间';

COMMENT ON TABLE tr_mi_recon_detail IS '医保对账每日对比-医保侧明细表(上传文件按结算日期聚合落库)';
COMMENT ON COLUMN tr_mi_recon_detail.id IS '主键(共用seq_tr_reports)';
COMMENT ON COLUMN tr_mi_recon_detail.file_id IS '来源上传记录tr_mi_recon_file.id';
COMMENT ON COLUMN tr_mi_recon_detail.insur_type IS '医保类型(与上传记录一致, 类型修改级联)';
COMMENT ON COLUMN tr_mi_recon_detail.insur_type_name IS '医保类型中文名(保存时落库, 类型修改级联)';
COMMENT ON COLUMN tr_mi_recon_detail.qty IS '对账数据量(同日多行已聚合)';
COMMENT ON COLUMN tr_mi_recon_detail.total_fee IS '医疗费总额(同日多行已聚合)';
COMMENT ON COLUMN tr_mi_recon_detail.settle_date IS '结算日期';
COMMENT ON COLUMN tr_mi_recon_detail.create_time IS '创建时间';
