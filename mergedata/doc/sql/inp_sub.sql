-- 住院现金统计子表
CREATE TABLE mpp_cash_statistics_inp_sub (
    -- 基础信息
    serial_no VARCHAR2(50) PRIMARY KEY,             -- 主键ID
    emp_id VARCHAR2(20),                            -- 收费员ID
    emp_name VARCHAR2(100),                         -- 收费员姓名

    -- 上午统计报表现金数据字段
    previous_day_advance_receipt NUMBER(18,2),      -- 前日暂收款
    today_advance_payment NUMBER(18,2),             -- 今日预交金数
    today_settlement_income NUMBER(18,2),           -- 今日结账收入
    today_pre_hospital_income NUMBER(18,2),         -- 今日院前收入
    traffic_assistance_fund NUMBER(18,2),           -- 交通救助金
    blood_donation_compensation NUMBER(18,2),       -- 无偿献血补偿金
    receivable_payable NUMBER(18,2),                -- 应收款/应付款
    today_report_total NUMBER(18,2),                -- 今日报表数合计
    previous_day_iou NUMBER(18,2),                  -- 前日欠条
    today_outpatient_iou NUMBER(18,2),              -- 今日门诊借条
    today_report_receivable_payable NUMBER(18,2),   -- 今日报表应收/应付

    -- 下午收取现金数据字段
    today_advance_receipt NUMBER(18,2),             -- 今日暂收款
    today_report_cash_received NUMBER(18,2),        -- 今日报表实收
    today_cash_received_total NUMBER(18,2),         -- 今日实收现金合计
    balance NUMBER(18,2),                           -- 余额
    adjustment NUMBER(18,2),                        -- 调整
    today_iou NUMBER(18,2),                         -- 今日欠条
    holiday_payment NUMBER(18,2),                   -- 节假日交款

    -- 收费员留存字段
    cash_on_hand NUMBER(18,2),                      -- 库存现金
    difference NUMBER(18,2),                        -- 差额
    remarks VARCHAR2(500),                          -- 备注

    -- 系统字段（建议添加）
    created_time TIMESTAMP DEFAULT SYSTIMESTAMP,    -- 创建时间
    updated_time TIMESTAMP DEFAULT SYSTIMESTAMP,    -- 更新时间
    created_by VARCHAR2(50),                        -- 创建人
    updated_by VARCHAR2(50)                         -- 更新人
);

-- 添加注释
COMMENT ON TABLE mpp_cash_statistics_inp_sub IS '住院现金统计子表';

-- 字段注释
COMMENT ON COLUMN mpp_cash_statistics_inp_sub.serial_no IS '主键ID/流水号';
COMMENT ON COLUMN mpp_cash_statistics_inp_sub.emp_id IS '收费员ID';
COMMENT ON COLUMN mpp_cash_statistics_inp_sub.emp_name IS '收费员姓名';

COMMENT ON COLUMN mpp_cash_statistics_inp_sub.previous_day_advance_receipt IS '前日暂收款';
COMMENT ON COLUMN mpp_cash_statistics_inp_sub.today_advance_payment IS '今日预交金数';
COMMENT ON COLUMN mpp_cash_statistics_inp_sub.today_settlement_income IS '今日结账收入';
COMMENT ON COLUMN mpp_cash_statistics_inp_sub.today_pre_hospital_income IS '今日院前收入';
COMMENT ON COLUMN mpp_cash_statistics_inp_sub.traffic_assistance_fund IS '交通救助金';
COMMENT ON COLUMN mpp_cash_statistics_inp_sub.blood_donation_compensation IS '无偿献血补偿金';
COMMENT ON COLUMN mpp_cash_statistics_inp_sub.receivable_payable IS '应收款/应付款';
COMMENT ON COLUMN mpp_cash_statistics_inp_sub.today_report_total IS '今日报表数合计';
COMMENT ON COLUMN mpp_cash_statistics_inp_sub.previous_day_iou IS '前日欠条';
COMMENT ON COLUMN mpp_cash_statistics_inp_sub.today_outpatient_iou IS '今日门诊借条';
COMMENT ON COLUMN mpp_cash_statistics_inp_sub.today_report_receivable_payable IS '今日报表应收/应付';

COMMENT ON COLUMN mpp_cash_statistics_inp_sub.today_advance_receipt IS '今日暂收款';
COMMENT ON COLUMN mpp_cash_statistics_inp_sub.today_report_cash_received IS '今日报表实收';
COMMENT ON COLUMN mpp_cash_statistics_inp_sub.today_cash_received_total IS '今日实收现金合计';
COMMENT ON COLUMN mpp_cash_statistics_inp_sub.balance IS '余额';
COMMENT ON COLUMN mpp_cash_statistics_inp_sub.adjustment IS '调整';
COMMENT ON COLUMN mpp_cash_statistics_inp_sub.today_iou IS '今日欠条';
COMMENT ON COLUMN mpp_cash_statistics_inp_sub.holiday_payment IS '节假日交款';

COMMENT ON COLUMN mpp_cash_statistics_inp_sub.cash_on_hand IS '库存现金';
COMMENT ON COLUMN mpp_cash_statistics_inp_sub.difference IS '差额';
COMMENT ON COLUMN mpp_cash_statistics_inp_sub.remarks IS '备注';

COMMENT ON COLUMN mpp_cash_statistics_inp_sub.created_time IS '创建时间';
COMMENT ON COLUMN mpp_cash_statistics_inp_sub.updated_time IS '更新时间';
COMMENT ON COLUMN mpp_cash_statistics_inp_sub.created_by IS '创建人';
COMMENT ON COLUMN mpp_cash_statistics_inp_sub.updated_by IS '更新人';

-- 创建索引
CREATE INDEX idx_mpp_cash_inp_sub_emp_id ON mpp_cash_statistics_inp_sub(emp_id);
CREATE INDEX idx_mpp_cash_inp_sub_created_time ON mpp_cash_statistics_inp_sub(created_time);

-- 创建序列（如果需要自增主键）
CREATE SEQUENCE seq_mpp_cash_inp_sub_id
    START WITH 1
    INCREMENT BY 1
    NOMAXVALUE
    NOCYCLE
    CACHE 20;