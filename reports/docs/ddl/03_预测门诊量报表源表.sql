-- ============================================================
-- 预测门诊量报表源表
-- ============================================================

-- 清理已存在对象(如重建请先执行)

DROP TABLE TR_FC_APPOINT CASCADE CONSTRAINTS;

DROP TABLE TR_FC_WEATHER CASCADE CONSTRAINTS;

DROP TABLE TR_FC_HOLIDAY CASCADE CONSTRAINTS;

-- 3.1.1 实时预约量表(每日抽取:各科室预约了某日就诊的号源数)
CREATE TABLE tr_fc_appoint (
    id              NUMBER(19)      PRIMARY KEY,
    appoint_date    DATE            NOT NULL,           -- 预约就诊日期
    dept_code       VARCHAR2(50),                       -- 科室编码(空表示全院汇总行)
    appoint_count   NUMBER(10)      DEFAULT 0,          -- 预约量
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 3.1.2 每日天气表
CREATE TABLE tr_fc_weather (
    id              NUMBER(19)      PRIMARY KEY,
    weather_date    DATE            NOT NULL,           -- 天气日期
    weather_type    VARCHAR2(20),                       -- 天气类型(晴/阴/雨/雪等)
    weather_coef    NUMBER(5,2),                        -- 天气出勤系数(空按1处理)
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 3.1.3 节假日表
CREATE TABLE tr_fc_holiday (
    id              NUMBER(19)      PRIMARY KEY,
    hol_date        DATE            NOT NULL,           -- 节假日日期
    hol_name        VARCHAR2(50),                       -- 节假日名称
    hol_type        VARCHAR2(20)    NOT NULL,           -- 类型(LEGAL_HOLIDAY法定节假日/WORKDAY_ADJUST调休上班)
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);

-- 创建索引

CREATE INDEX idx_tr_fc_appoint_date ON tr_fc_appoint(appoint_date);
CREATE UNIQUE INDEX uk_tr_fc_weather_date ON tr_fc_weather(weather_date);
CREATE UNIQUE INDEX uk_tr_fc_holiday_date ON tr_fc_holiday(hol_date);

-- 添加注释

COMMENT ON TABLE tr_fc_appoint IS '预测门诊量-实时预约量源表(各科室每日预约了某日就诊的号源数)';
COMMENT ON COLUMN tr_fc_appoint.id IS '主键ID';
COMMENT ON COLUMN tr_fc_appoint.appoint_date IS '预约就诊日期';
COMMENT ON COLUMN tr_fc_appoint.dept_code IS '科室编码(空表示全院汇总行)';
COMMENT ON COLUMN tr_fc_appoint.appoint_count IS '预约量';
COMMENT ON COLUMN tr_fc_appoint.create_time IS '创建时间';
COMMENT ON COLUMN tr_fc_appoint.update_time IS '更新时间';
COMMENT ON COLUMN tr_fc_appoint.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_fc_appoint.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_fc_appoint.ext3 IS '扩展字段3';
COMMENT ON TABLE tr_fc_weather IS '预测门诊量-每日天气源表(天气类型及天气出勤系数)';
COMMENT ON COLUMN tr_fc_weather.id IS '主键ID';
COMMENT ON COLUMN tr_fc_weather.weather_date IS '天气日期';
COMMENT ON COLUMN tr_fc_weather.weather_type IS '天气类型(晴/阴/雨/雪等)';
COMMENT ON COLUMN tr_fc_weather.weather_coef IS '天气出勤系数(空按1处理)';
COMMENT ON COLUMN tr_fc_weather.create_time IS '创建时间';
COMMENT ON COLUMN tr_fc_weather.update_time IS '更新时间';
COMMENT ON COLUMN tr_fc_weather.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_fc_weather.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_fc_weather.ext3 IS '扩展字段3';
COMMENT ON TABLE tr_fc_holiday IS '预测门诊量-节假日源表(法定节假日/调休上班,用于节假日系数)';
COMMENT ON COLUMN tr_fc_holiday.id IS '主键ID';
COMMENT ON COLUMN tr_fc_holiday.hol_date IS '节假日日期';
COMMENT ON COLUMN tr_fc_holiday.hol_name IS '节假日名称';
COMMENT ON COLUMN tr_fc_holiday.hol_type IS '类型(LEGAL_HOLIDAY法定节假日/WORKDAY_ADJUST调休上班)';
COMMENT ON COLUMN tr_fc_holiday.create_time IS '创建时间';
COMMENT ON COLUMN tr_fc_holiday.update_time IS '更新时间';
COMMENT ON COLUMN tr_fc_holiday.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_fc_holiday.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_fc_holiday.ext3 IS '扩展字段3';
