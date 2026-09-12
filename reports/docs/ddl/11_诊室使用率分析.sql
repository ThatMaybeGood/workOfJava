-- ============================================================
-- 诊室使用率分析
-- ============================================================

-- 清理已存在对象(如重建请先执行)

DROP TABLE tr_room_use_ov CASCADE CONSTRAINTS;

DROP TABLE tr_room_use_dtl CASCADE CONSTRAINTS;

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
    dept_code       VARCHAR2(50),                       -- 科室编码(RoomUsageMapper 与 TR_DEPT_DICT 关联用)
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

-- 创建索引

CREATE INDEX idx_tr_room_overview_date ON tr_room_use_ov(stat_date);
CREATE INDEX idx_tr_room_detail_dept ON tr_room_use_dtl(dept_code);
CREATE UNIQUE INDEX uk_tr_room_use_dtl ON tr_room_use_dtl(stat_date, dept_code);

-- 添加注释

COMMENT ON TABLE tr_room_use_ov IS '诊室使用率分析-概览';
COMMENT ON TABLE tr_room_use_ov IS '诊室使用率分析-概览(诊室使用率的总览指标:平均使用率、上午/下午/节假日使用率)';
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
COMMENT ON TABLE tr_room_use_dtl IS '诊室使用率分析-科室明细';
COMMENT ON TABLE tr_room_use_dtl IS '诊室使用率分析-科室明细(按科室维度统计诊室使用率)';
COMMENT ON COLUMN tr_room_use_dtl.id IS '主键ID';
COMMENT ON COLUMN tr_room_use_dtl.stat_date IS '统计日期';
COMMENT ON COLUMN tr_room_use_dtl.dept_code IS '科室编码(关联TR_DEPT_DICT)';
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
