-- 住院现金统计子表（与 InpCashSubEntity 代码一致）
DROP TABLE mpp_cash_inp_sub;

CREATE TABLE mpp_cash_inp_sub (
    serial_no VARCHAR2(50) NOT NULL,            -- 主表流水号（关联mpp_cash_inp_master）
    emp_id VARCHAR2(20),                        -- 收费员ID
    emp_name VARCHAR2(100),                     -- 收费员姓名
    prev_day_adv_receipt NUMBER(18,2),          -- （1）前日暂收款
    today_adv_payment NUMBER(18,2),             -- （2）今日预交金数（HIS收入）
    today_settle_income NUMBER(18,2),           -- （3）今日结账收入（HIS收入）
    today_pre_hosp_income NUMBER(18,2),         -- （4）今日院前收入（HIS收入）
    traffic_assist_fund NUMBER(18,2),           -- （5）交通救助金
    blood_donate_compensate NUMBER(18,2),       -- （6）无偿献血补偿金
    receivable_payable NUMBER(18,2),            -- （7）应收款/应付款
    today_report_total NUMBER(18,2),            -- （8）今日报表数合计（公式）
    prev_day_iou NUMBER(18,2),                  -- （9）前日欠条
    today_outp_iou NUMBER(18,2),                -- （10）今日门诊借条
    today_report_rec_pay NUMBER(18,2),          -- （11）今日报表应收/应付（公式）
    today_adv_receipt NUMBER(18,2),             -- （12）今日暂收款
    today_report_cash_rcv NUMBER(18,2),         -- （13）今日报表实收
    today_cash_rcv_total NUMBER(18,2),          -- （14）今日实收现金合计（公式）
    balance NUMBER(18,2),                       -- （15）余额（公式）
    adjustment NUMBER(18,2),                    -- （16）调整
    today_iou NUMBER(18,2),                     -- （17）今日欠条（公式）
    holiday_payment NUMBER(18,2),               -- （18）节假日交款
    cash_on_hand NUMBER(18,2),                  -- （19）库存现金
    difference NUMBER(18,2),                    -- （20）差额（公式）
    remarks VARCHAR2(500),                      -- （21）备注
    created_time TIMESTAMP DEFAULT SYSTIMESTAMP,-- 创建时间
    updated_time TIMESTAMP DEFAULT SYSTIMESTAMP,-- 更新时间
    created_by VARCHAR2(50),                    -- 创建人
    updated_by VARCHAR2(50),                    -- 更新人
    db_user VARCHAR2(50)                        -- 操作员数据库账号
);

COMMENT ON TABLE mpp_cash_inp_sub IS '住院现金统计子表';
COMMENT ON COLUMN mpp_cash_inp_sub.serial_no IS '主表流水号（关联mpp_cash_inp_master.serial_no，一个主表对应多行子表）';
COMMENT ON COLUMN mpp_cash_inp_sub.emp_id IS '收费员ID';
COMMENT ON COLUMN mpp_cash_inp_sub.emp_name IS '收费员姓名';
COMMENT ON COLUMN mpp_cash_inp_sub.prev_day_adv_receipt IS '（1）前日暂收款';
COMMENT ON COLUMN mpp_cash_inp_sub.today_adv_payment IS '（2）今日预交金数（HIS收入）';
COMMENT ON COLUMN mpp_cash_inp_sub.today_settle_income IS '（3）今日结账收入（HIS收入）';
COMMENT ON COLUMN mpp_cash_inp_sub.today_pre_hosp_income IS '（4）今日院前收入（HIS收入）';
COMMENT ON COLUMN mpp_cash_inp_sub.traffic_assist_fund IS '（5）交通救助金';
COMMENT ON COLUMN mpp_cash_inp_sub.blood_donate_compensate IS '（6）无偿献血补偿金';
COMMENT ON COLUMN mpp_cash_inp_sub.receivable_payable IS '（7）应收款/应付款';
COMMENT ON COLUMN mpp_cash_inp_sub.today_report_total IS '（8）今日报表数合计（公式）';
COMMENT ON COLUMN mpp_cash_inp_sub.prev_day_iou IS '（9）前日欠条';
COMMENT ON COLUMN mpp_cash_inp_sub.today_outp_iou IS '（10）今日门诊借条';
COMMENT ON COLUMN mpp_cash_inp_sub.today_report_rec_pay IS '（11）今日报表应收/应付（公式）';
COMMENT ON COLUMN mpp_cash_inp_sub.today_adv_receipt IS '（12）今日暂收款';
COMMENT ON COLUMN mpp_cash_inp_sub.today_report_cash_rcv IS '（13）今日报表实收';
COMMENT ON COLUMN mpp_cash_inp_sub.today_cash_rcv_total IS '（14）今日实收现金合计（公式）';
COMMENT ON COLUMN mpp_cash_inp_sub.balance IS '（15）余额（公式）';
COMMENT ON COLUMN mpp_cash_inp_sub.adjustment IS '（16）调整';
COMMENT ON COLUMN mpp_cash_inp_sub.today_iou IS '（17）今日欠条（公式）';
COMMENT ON COLUMN mpp_cash_inp_sub.holiday_payment IS '（18）节假日交款';
COMMENT ON COLUMN mpp_cash_inp_sub.cash_on_hand IS '（19）库存现金';
COMMENT ON COLUMN mpp_cash_inp_sub.difference IS '（20）差额（公式）';
COMMENT ON COLUMN mpp_cash_inp_sub.remarks IS '备注';
COMMENT ON COLUMN mpp_cash_inp_sub.created_time IS '创建时间';
COMMENT ON COLUMN mpp_cash_inp_sub.updated_time IS '更新时间';
COMMENT ON COLUMN mpp_cash_inp_sub.created_by IS '创建人';
COMMENT ON COLUMN mpp_cash_inp_sub.updated_by IS '更新人';
COMMENT ON COLUMN mpp_cash_inp_sub.db_user IS '操作员数据库账号';

CREATE INDEX idx_mpp_cash_inp_sub_serial ON mpp_cash_inp_sub(serial_no);
