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

-- ============================================================
-- 2026-11-05 v2: 上传记录加启用状态列——同类型同数据日期重复上传时, 旧记录自动标记作废, 最新一条启用
-- ============================================================
-- 幂等: 列已存在则跳过
DECLARE
    v_cnt NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_cnt FROM user_tab_columns
     WHERE table_name = 'TR_MI_RECON_FILE' AND column_name = 'STATUS';
    IF v_cnt = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE tr_mi_recon_file ADD status CHAR(1) DEFAULT ''1'' NOT NULL';
    END IF;
END;
/
-- 存量数据全部视为启用
UPDATE tr_mi_recon_file SET status = '1' WHERE status IS NULL OR status NOT IN ('0', '1');
COMMIT;

COMMENT ON COLUMN tr_mi_recon_file.status IS '启用状态: 1启用 0已作废(同类型同数据日期重复上传时旧记录自动作废)';

-- 验证: 第1条应返回1, 第2条按status分组统计
SELECT COUNT(*) AS status_col_exists FROM user_tab_columns
 WHERE table_name = 'TR_MI_RECON_FILE' AND column_name = 'STATUS';
SELECT status, COUNT(*) AS cnt FROM tr_mi_recon_file GROUP BY status;

-- ============================================================
-- 2026-11-05 v3: 支持上传历史版本切换启用——明细不再物理删除, 多版本共存, 生效性由file.status控制
-- ============================================================
-- 1) 放开明细表(类型+结算日期)唯一约束: 多个版本的明细可共存, 同一类型同一天只应有一个启用版本
DECLARE
    v_cnt NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_cnt FROM user_constraints
     WHERE constraint_name = 'UK_TR_MI_RECON_DETAIL_TD' AND table_name = 'TR_MI_RECON_DETAIL';
    IF v_cnt > 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE tr_mi_recon_detail DROP CONSTRAINT uk_tr_mi_recon_detail_td';
    END IF;
END;
/
COMMIT;

-- 验证: 应返回0(约束已不存在)
SELECT COUNT(*) AS uk_exists FROM user_constraints
 WHERE constraint_name = 'UK_TR_MI_RECON_DETAIL_TD' AND table_name = 'TR_MI_RECON_DETAIL';
