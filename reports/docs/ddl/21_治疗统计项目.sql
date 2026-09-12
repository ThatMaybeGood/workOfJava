-- ============================================================
-- 治疗统计报表 - 治疗项目维度
-- ============================================================
-- 「TOP10 治疗项目」这块图表需要项目级数据，tr_treat_stat_ov / tr_treat_stat_dtl
-- 都只到科室维度，所以单独加这一张按项目的表。

-- 清理已存在对象(如重建请先执行)

DROP TABLE tr_treat_stat_item CASCADE CONSTRAINTS;

CREATE TABLE tr_treat_stat_item (
    id               NUMBER(19)      PRIMARY KEY,
    stat_date        DATE            NOT NULL,           -- 统计日期
    item_name        VARCHAR2(100)   NOT NULL,           -- 治疗项目名称
    treatment_count  NUMBER(10)      DEFAULT 0,          -- 治疗人次
    treatment_amount NUMBER(18,2),                       -- 治疗金额
    create_time      DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time      DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1             VARCHAR2(500),                        -- 扩展字段1
    ext2             VARCHAR2(500),                        -- 扩展字段2
    ext3             VARCHAR2(500)                         -- 扩展字段3
);

CREATE INDEX idx_tr_treat_item_date ON tr_treat_stat_item(stat_date);
CREATE UNIQUE INDEX uk_tr_treat_stat_item ON tr_treat_stat_item(stat_date, item_name);

COMMENT ON TABLE tr_treat_stat_item IS '治疗统计报表-治疗项目明细(按项目统计治疗人次与金额, 用于 TOP10 项目图)';
COMMENT ON COLUMN tr_treat_stat_item.stat_date IS '统计日期';
COMMENT ON COLUMN tr_treat_stat_item.item_name IS '治疗项目名称';
COMMENT ON COLUMN tr_treat_stat_item.treatment_count IS '治疗人次';
COMMENT ON COLUMN tr_treat_stat_item.treatment_amount IS '治疗金额';
COMMENT ON COLUMN tr_treat_stat_item.create_time IS '创建时间';
COMMENT ON COLUMN tr_treat_stat_item.update_time IS '更新时间';
COMMENT ON COLUMN tr_treat_stat_item.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_treat_stat_item.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_treat_stat_item.ext3 IS '扩展字段3';
