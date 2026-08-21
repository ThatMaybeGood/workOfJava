# 门诊财务报表 — 简化抽取存储设计

## 目标

原有报表直接查询 HIS 源表（`outp_rcpt_master` / `outp_acct_master` / `clinic_master` / `outp_payments_money` / `cq_card.mop_timeschedule_queue`），由 5 个存储过程（`SP_GetIndicator_24` / `SP_GetIndicatorDetailList_25` / `SP_GetBarList_26` / `SP_GetPieList_27` / `SP_GetDateRange`）完成全部统计。

现改造为：**ETL 按日期抽取「原始明细」到目的库，报表 Java 在查询时完成统计逻辑（人次去重、汇总、同比、饼图归类）**。目的库只保留 5 张抽取表，不存任何预聚合/去年同期数据。

## 抽取方式

ETL 传入一个日期 D，抽取当天数据，**先 DELETE 后 INSERT（幂等，可重跑）**：

| 表 | 幂等删除键 | 抽取来源 |
|---|---|---|
| `ETL_OUTP_RCPT` | `DELETE WHERE VISIT_DATE = D` | `outp_rcpt_master` |
| `ETL_OUTP_ACCT` | `DELETE WHERE ACCT_DATE = D` | `outp_acct_master` |
| `ETL_CLINIC` | `DELETE WHERE VISIT_DATE = D` | `clinic_master` |
| `ETL_OUTP_PAYMENT` | `DELETE WHERE VISIT_DATE = D` | `outp_payments_money` 关联收据表补日期 |
| `ETL_TIMESCHEDULE_QUEUE` | `DELETE WHERE VISIT_DATE = D` | `cq_card.mop_timeschedule_queue` 关联挂号表补日期 |

## 五张抽取表

### 1. `ETL_OUTP_RCPT` — 门诊收据明细（← `outp_rcpt_master`）

| 列名 | 源列 | 类型 | 用途 |
|---|---|---|---|
| ID | — | NUMBER(19) PK | 主键 |
| RCPT_NO | RCPT_NO | VARCHAR2(40) | 人次去重（Java 解析前缀+序号）、关联支付表 |
| PATIENT_ID | PATIENT_ID | VARCHAR2(40) | 人次去重 |
| VISIT_DATE | VISIT_DATE | DATE | 抽取日期、当期/同期判定 |
| TOTAL_CHARGES | TOTAL_CHARGES | NUMBER(18,2) | bt6 渠道金额、bt8 业务类型金额 |
| REFUNDED_RCPT_NO | REFUNDED_RCPT_NO | VARCHAR2(40) | 退项(T3)筛选 `IS NOT NULL` |
| OPERATOR_NO | OPERATOR_NO | VARCHAR2(20) | bt1/3/4/6 的 operator 归类 |
| BILL_CLASS | BILL_CLASS | VARCHAR2(10) | bt8 分界后 挂号/缴费 判定 |
| CREATE_TIME | — | DATE | 审计 |
| UPDATE_TIME | — | DATE | 审计 |

### 2. `ETL_OUTP_ACCT` — 门诊结账汇总（← `outp_acct_master`）—— **收据张数 / 金额唯一口径**

| 列名 | 源列 | 类型 | 用途 |
|---|---|---|---|
| ID | — | NUMBER(19) PK | 主键 |
| ACCT_DATE | ACCT_DATE | DATE | 抽取日期、当期/同期判定 |
| TOTAL_COSTS | TOTAL_COSTS | NUMBER(18,2) | T1 金额 |
| REFUND_AMOUNT | REFUND_AMOUNT | NUMBER(18,2) | T3 金额 |
| RCPTS_NUM | RCPTS_NUM | NUMBER(12) | T1 收据张数 |
| REFUND_NUM | REFUND_NUM | NUMBER(12) | T3 收据张数 |
| CREATE_TIME | — | DATE | 审计 |
| UPDATE_TIME | — | DATE | 审计 |

Java 组合口径：T1 金额=`SUM(TOTAL_COSTS)`，T2=`SUM(TOTAL_COSTS)+SUM(REFUND_AMOUNT)`，T3=`SUM(REFUND_AMOUNT)`；收据张数同理 `RCPTS_NUM` / `RCPTS_NUM+REFUND_NUM` / `REFUND_NUM`。

### 3. `ETL_CLINIC` — 门诊挂号明细（← `clinic_master`）

| 列名 | 源列 | 类型 | 用途 |
|---|---|---|---|
| ID | — | NUMBER(19) PK | 主键 |
| PATIENT_ID | PATIENT_ID | VARCHAR2(40) | bt2 关联排班表 |
| VISIT_DATE | VISIT_DATE | DATE | 门诊量、抽取日期 |
| RETURNED_DATE | RETURNED_DATE | DATE | 门诊量 T2/T3 退号筛选 |
| REGIST_FEE | REGIST_FEE | NUMBER(18,2) | bt8 分界前挂号费 |
| CLINIC_FEE | CLINIC_FEE | NUMBER(18,2) | bt8 分界前挂号费 |
| CLINIC_LABEL | CLINIC_LABEL | VARCHAR2(40) | bt2 拼接 `schedule_id` |
| VISIT_TIME_DESC | VISIT_TIME_DESC | VARCHAR2(40) | bt2 拼接 `schedule_id` |
| OPERATOR_NO | OPERATOR_NO | VARCHAR2(20) | bt2 归类 |
| CREATE_TIME | — | DATE | 审计 |
| UPDATE_TIME | — | DATE | 审计 |

### 4. `ETL_OUTP_PAYMENT` — 门诊支付明细（← `outp_payments_money`）

源表无日期列，抽取时关联 `outp_rcpt_master.RCPT_NO`，取当天收据对应的支付行，冗余存 `VISIT_DATE`。

| 列名 | 源列 | 类型 | 用途 |
|---|---|---|---|
| ID | — | NUMBER(19) PK | 主键 |
| RCPT_NO | RCPT_NO | VARCHAR2(40) | 关联收据 |
| PATIENT_ID | （关联收据得） | VARCHAR2(40) | bt5 人次支付 同一患者同一收据 去重 |
| MONEY_TYPE | MONEY_TYPE | VARCHAR2(30) | bt5/7/9 类别 |
| PAYMENT_AMOUNT | PAYMENT_AMOUNT | NUMBER(18,2) | bt7/9 实收金额 |
| REFUNDED_AMOUNT | REFUNDED_AMOUNT | NUMBER(18,2) | bt7/9 退费金额 |
| VISIT_DATE | （关联收据得） | DATE | 冗余抽取日期 |
| CREATE_TIME | — | DATE | 审计 |
| UPDATE_TIME | — | DATE | 审计 |

### 5. `ETL_TIMESCHEDULE_QUEUE` — 排班队列（← `cq_card.mop_timeschedule_queue`，仅 bt2 取号渠道用）

源表无日期列，抽取时关联 `clinic_master`（`schedule_id = clinic_label||'|'||yyyyMMdd||'|'||visit_time_desc` 且 `patient_id` 匹配），冗余存 `VISIT_DATE`。

| 列名 | 源列 | 类型 | 用途 |
|---|---|---|---|
| ID | — | NUMBER(19) PK | 主键 |
| SCHEDULE_ID | SCHEDULE_ID | VARCHAR2(100) | 与挂号拼接串比对 |
| PATIENT_ID | PATIENT_ID | VARCHAR2(40) | 关联 |
| QUEUE_TYPE | QUEUE_TYPE | VARCHAR2(20) | 筛选 `='reserve'` |
| IS_USED | IS_USED | VARCHAR2(20) | 筛选 `='used'` |
| VISIT_DATE | （关联挂号得） | DATE | 冗余抽取日期 |
| CREATE_TIME | — | DATE | 审计 |
| UPDATE_TIME | — | DATE | 审计 |

## 指标 → 数据来源映射

| 指标 | 数据来源 | 统计类型筛选 |
|---|---|---|
| 门诊量 | `ETL_CLINIC` | T1 全量 / T2 `RETURNED_DATE IS NULL` / T3 `RETURNED_DATE IS NOT NULL` |
| 缴费人次 | `ETL_OUTP_RCPT`（Java 去重） | T1 全量 / T2 `TOTAL_CHARGES>=0` / T3 `REFUNDED_RCPT_NO IS NOT NULL AND TOTAL_CHARGES<0` |
| 收据张数 / 金额 | `ETL_OUTP_ACCT` | 见上方 Java 组合口径 |
| bt1 订单来源 / bt3 订单渠道 | `ETL_OUTP_RCPT`（COUNT） | 收据筛选同上 |
| bt2 取号渠道 | `ETL_CLINIC ⋈ ETL_TIMESCHEDULE_QUEUE`（COUNT） | 挂号筛选 + `QUEUE_TYPE='reserve'` `IS_USED='used'` |
| bt4 缴费人次渠道 | `ETL_OUTP_RCPT`（Java 去重） | 收据筛选 |
| bt5 缴费人次支付方式 | `ETL_OUTP_RCPT ⋈ ETL_OUTP_PAYMENT`（Java 去重） | 收据筛选 |
| bt6 渠道金额 | `ETL_OUTP_RCPT`（SUM TOTAL_CHARGES） | 收据筛选 |
| bt7 支付方式金额 | `ETL_OUTP_PAYMENT`（SUM） | T1 `PAYMENT−REFUNDED` / T2 `PAYMENT` / T3 `REFUNDED` |
| bt8 业务类型金额 | `ETL_CLINIC` + `ETL_OUTP_RCPT` | 分界点见下 |
| bt9 应收 / 实收 | `ETL_OUTP_PAYMENT`（SUM） | 同 bt7 |
| bt10 应收金额（新增） | **类别待定，占位** | — |

## Java 统计逻辑要点

- **人次去重**：Java 解析 `RCPT_NO` 前缀 + 序号（纯数字 8 位 / 字母+7 位 / 两字母+6 位），同一病人、同前缀、序号连续递增的收据算 1 人次（对应源过程 `lag()` 逻辑）。
- **同比**：同一套查询对「日期区间 −12 个月」再执行一次，按周期（月/天）或类别对齐后计算增长率——**表内无 last_year 列**。
- **bt8 分界点 `2025-02-08`**：分界前挂号费取 `ETL_CLINIC.REGIST_FEE+CLINIC_FEE`（当日挂号），缴费取 `ETL_OUTP_RCPT.TOTAL_CHARGES`（门诊缴费）；分界后按 `BILL_CLASS='1'` → 当日挂号，其余 → 门诊缴费。
- **operator 归类映射**：`'9101'` → 自助机，`'C746'` → 线上退费，其他 → 窗口。
- **bt9 类别映射**：`MONEY_TYPE IN ('医改统筹','医院垫支')` → 医改统筹/医院垫支；`IN ('支付宝','现金','信用卡','银行卡','微信','自助POS','支票')` → 实收金额；其余 → 应收账款。
- **bt10 应收金额**：具体类别清单由业务后续补充；在类别明确前，报表返回空，前端第 5 个饼图正常渲染。

## 报表接口请求

请求 body（网关 `reports.cash.outpatient-finance`）：

```json
{ "statisticType": 1, "timeType": 1, "startDate": "2026-04", "endDate": "2026-04" }
```

- `statisticType`：1 汇总 / 2 进项 / 3 退项
- `timeType`：1 按月 / 2 按天
- `statisticType=1` 时同时返回 bt1~bt10 饼图，其中第 5 个「应收金额」饼图为 `business_type=10`。

## 建表 DDL（Oracle）

```sql
-- ============================================================
-- 1. ETL_OUTP_RCPT 门诊收据明细（← outp_rcpt_master）
-- ============================================================
CREATE TABLE ETL_OUTP_RCPT (
    ID              NUMBER(19)        NOT NULL,
    RCPT_NO         VARCHAR2(40)      NOT NULL,
    PATIENT_ID      VARCHAR2(40),
    VISIT_DATE      DATE              NOT NULL,
    TOTAL_CHARGES   NUMBER(18,2),
    REFUNDED_RCPT_NO VARCHAR2(40),
    OPERATOR_NO     VARCHAR2(20),
    BILL_CLASS      VARCHAR2(10),
    CREATE_TIME     DATE              DEFAULT SYSDATE,
    UPDATE_TIME     DATE              DEFAULT SYSDATE,
    CONSTRAINT PK_ETL_OUTP_RCPT PRIMARY KEY (ID)
);
COMMENT ON TABLE  ETL_OUTP_RCPT        IS '门诊财务报表-抽取：门诊收据明细（源表 outp_rcpt_master）';
COMMENT ON COLUMN ETL_OUTP_RCPT.ID               IS '主键ID';
COMMENT ON COLUMN ETL_OUTP_RCPT.RCPT_NO          IS '收据号（人次去重解析前缀+序号、关联支付表）';
COMMENT ON COLUMN ETL_OUTP_RCPT.PATIENT_ID       IS '患者ID（人次去重）';
COMMENT ON COLUMN ETL_OUTP_RCPT.VISIT_DATE       IS '就诊日期（抽取日期、当期/同期判定）';
COMMENT ON COLUMN ETL_OUTP_RCPT.TOTAL_CHARGES    IS '金额（bt6 渠道金额、bt8 业务类型金额）';
COMMENT ON COLUMN ETL_OUTP_RCPT.REFUNDED_RCPT_NO IS '退费关联收据号（退项 T3 筛选）';
COMMENT ON COLUMN ETL_OUTP_RCPT.OPERATOR_NO      IS '操作员号（bt1/3/4/6 operator 归类）';
COMMENT ON COLUMN ETL_OUTP_RCPT.BILL_CLASS       IS '单据类别（bt8 分界后 挂号/缴费 判定）';
CREATE INDEX IDX_ETL_OUTP_RCPT_VISIT ON ETL_OUTP_RCPT (VISIT_DATE);
CREATE INDEX IDX_ETL_OUTP_RCPT_PATIENT_RCPT ON ETL_OUTP_RCPT (PATIENT_ID, RCPT_NO);

-- ============================================================
-- 2. ETL_OUTP_ACCT 门诊结账汇总（← outp_acct_master）收据张数/金额唯一口径
-- ============================================================
CREATE TABLE ETL_OUTP_ACCT (
    ID            NUMBER(19)        NOT NULL,
    ACCT_DATE     DATE              NOT NULL,
    TOTAL_COSTS   NUMBER(18,2),
    REFUND_AMOUNT NUMBER(18,2),
    RCPTS_NUM     NUMBER(12),
    REFUND_NUM    NUMBER(12),
    CREATE_TIME   DATE              DEFAULT SYSDATE,
    UPDATE_TIME   DATE              DEFAULT SYSDATE,
    CONSTRAINT PK_ETL_OUTP_ACCT PRIMARY KEY (ID)
);
COMMENT ON TABLE  ETL_OUTP_ACCT        IS '门诊财务报表-抽取：门诊结账汇总（源表 outp_acct_master，收据张数/金额唯一口径）';
COMMENT ON COLUMN ETL_OUTP_ACCT.ID            IS '主键ID';
COMMENT ON COLUMN ETL_OUTP_ACCT.ACCT_DATE     IS '结账日期（抽取日期、当期/同期判定）';
COMMENT ON COLUMN ETL_OUTP_ACCT.TOTAL_COSTS   IS '总费用（T1 金额）';
COMMENT ON COLUMN ETL_OUTP_ACCT.REFUND_AMOUNT IS '退费金额（T3 金额）';
COMMENT ON COLUMN ETL_OUTP_ACCT.RCPTS_NUM     IS '收据张数（T1）';
COMMENT ON COLUMN ETL_OUTP_ACCT.REFUND_NUM    IS '退费张数（T3）';
CREATE INDEX IDX_ETL_OUTP_ACCT_DATE ON ETL_OUTP_ACCT (ACCT_DATE);

-- ============================================================
-- 3. ETL_CLINIC 门诊挂号明细（← clinic_master）
-- ============================================================
CREATE TABLE ETL_CLINIC (
    ID             NUMBER(19)        NOT NULL,
    PATIENT_ID     VARCHAR2(40),
    VISIT_DATE     DATE              NOT NULL,
    RETURNED_DATE  DATE,
    REGIST_FEE     NUMBER(18,2),
    CLINIC_FEE     NUMBER(18,2),
    CLINIC_LABEL   VARCHAR2(40),
    VISIT_TIME_DESC VARCHAR2(40),
    OPERATOR_NO    VARCHAR2(20),
    CREATE_TIME    DATE              DEFAULT SYSDATE,
    UPDATE_TIME    DATE              DEFAULT SYSDATE,
    CONSTRAINT PK_ETL_CLINIC PRIMARY KEY (ID)
);
COMMENT ON TABLE  ETL_CLINIC         IS '门诊财务报表-抽取：门诊挂号明细（源表 clinic_master）';
COMMENT ON COLUMN ETL_CLINIC.ID             IS '主键ID';
COMMENT ON COLUMN ETL_CLINIC.PATIENT_ID     IS '患者ID（bt2 关联排班表）';
COMMENT ON COLUMN ETL_CLINIC.VISIT_DATE     IS '就诊日期（门诊量、抽取日期）';
COMMENT ON COLUMN ETL_CLINIC.RETURNED_DATE  IS '退号日期（门诊量 T2/T3 退号筛选）';
COMMENT ON COLUMN ETL_CLINIC.REGIST_FEE     IS '挂号费（bt8 分界前挂号费）';
COMMENT ON COLUMN ETL_CLINIC.CLINIC_FEE     IS '诊查费（bt8 分界前挂号费）';
COMMENT ON COLUMN ETL_CLINIC.CLINIC_LABEL   IS '诊室标签（bt2 拼接 schedule_id）';
COMMENT ON COLUMN ETL_CLINIC.VISIT_TIME_DESC IS '就诊时段（bt2 拼接 schedule_id）';
COMMENT ON COLUMN ETL_CLINIC.OPERATOR_NO    IS '操作员号（bt2 operator 归类）';
CREATE INDEX IDX_ETL_CLINIC_VISIT ON ETL_CLINIC (VISIT_DATE);

-- ============================================================
-- 4. ETL_OUTP_PAYMENT 门诊支付明细（← outp_payments_money，无日期列，抽取时关联收据表补 VISIT_DATE）
-- ============================================================
CREATE TABLE ETL_OUTP_PAYMENT (
    ID               NUMBER(19)        NOT NULL,
    RCPT_NO          VARCHAR2(40)      NOT NULL,
    PATIENT_ID       VARCHAR2(40),
    MONEY_TYPE       VARCHAR2(30),
    PAYMENT_AMOUNT   NUMBER(18,2),
    REFUNDED_AMOUNT  NUMBER(18,2),
    VISIT_DATE       DATE,
    CREATE_TIME      DATE              DEFAULT SYSDATE,
    UPDATE_TIME      DATE              DEFAULT SYSDATE,
    CONSTRAINT PK_ETL_OUTP_PAYMENT PRIMARY KEY (ID)
);
COMMENT ON TABLE  ETL_OUTP_PAYMENT      IS '门诊财务报表-抽取：门诊支付方式明细（源表 outp_payments_money）';
COMMENT ON COLUMN ETL_OUTP_PAYMENT.ID              IS '主键ID';
COMMENT ON COLUMN ETL_OUTP_PAYMENT.RCPT_NO         IS '关联收据号';
COMMENT ON COLUMN ETL_OUTP_PAYMENT.PATIENT_ID      IS '患者ID（bt5 人次支付去重，抽取时关联收据表补）';
COMMENT ON COLUMN ETL_OUTP_PAYMENT.MONEY_TYPE      IS '支付方式（bt5/7/9 类别）';
COMMENT ON COLUMN ETL_OUTP_PAYMENT.PAYMENT_AMOUNT  IS '实收金额（bt7/9）';
COMMENT ON COLUMN ETL_OUTP_PAYMENT.REFUNDED_AMOUNT IS '退费金额（bt7/9）';
COMMENT ON COLUMN ETL_OUTP_PAYMENT.VISIT_DATE      IS '冗余就诊日期（抽取时关联收据表补）';
CREATE INDEX IDX_ETL_OUTP_PAYMENT_VISIT ON ETL_OUTP_PAYMENT (VISIT_DATE);
CREATE INDEX IDX_ETL_OUTP_PAYMENT_RCPT  ON ETL_OUTP_PAYMENT (RCPT_NO);

-- ============================================================
-- 5. ETL_TIMESCHEDULE_QUEUE 排班队列（← cq_card.mop_timeschedule_queue，仅 bt2 取号渠道用）
--    源表无日期列，抽取时关联 clinic_master 补 VISIT_DATE
-- ============================================================
CREATE TABLE ETL_TIMESCHEDULE_QUEUE (
    ID          NUMBER(19)        NOT NULL,
    SCHEDULE_ID VARCHAR2(100),
    PATIENT_ID  VARCHAR2(40),
    QUEUE_TYPE  VARCHAR2(20),
    IS_USED     VARCHAR2(20),
    VISIT_DATE  DATE,
    CREATE_TIME DATE              DEFAULT SYSDATE,
    UPDATE_TIME DATE              DEFAULT SYSDATE,
    CONSTRAINT PK_ETL_TIMESCHEDULE_QUEUE PRIMARY KEY (ID)
);
COMMENT ON TABLE  ETL_TIMESCHEDULE_QUEUE       IS '门诊财务报表-抽取：排班队列（源表 cq_card.mop_timeschedule_queue，仅 bt2 取号渠道分析使用）';
COMMENT ON COLUMN ETL_TIMESCHEDULE_QUEUE.ID          IS '主键ID';
COMMENT ON COLUMN ETL_TIMESCHEDULE_QUEUE.SCHEDULE_ID IS '排班ID（与 clinic_label|yyyyMMdd|visit_time_desc 拼接比对）';
COMMENT ON COLUMN ETL_TIMESCHEDULE_QUEUE.PATIENT_ID  IS '患者ID（关联挂号）';
COMMENT ON COLUMN ETL_TIMESCHEDULE_QUEUE.QUEUE_TYPE  IS '队列类型（筛选 reserve）';
COMMENT ON COLUMN ETL_TIMESCHEDULE_QUEUE.IS_USED     IS '是否使用（筛选 used）';
COMMENT ON COLUMN ETL_TIMESCHEDULE_QUEUE.VISIT_DATE  IS '冗余就诊日期（抽取时关联挂号表补）';
CREATE INDEX IDX_ETL_TIMESCHEDULE_QUEUE_SCHEDULE ON ETL_TIMESCHEDULE_QUEUE (SCHEDULE_ID);
CREATE INDEX IDX_ETL_TIMESCHEDULE_QUEUE_VISIT    ON ETL_TIMESCHEDULE_QUEUE (VISIT_DATE);
```

> **ID 填充说明**：ETL 抽取时若直接用 SQL `INSERT` 需自行生成 ID，可任选其一——
> ① 建一个共享序列：`CREATE SEQUENCE SEQ_ETL_FINANCE_ID START WITH 1 INCREMENT BY 1;`，插入时 `SEQ_ETL_FINANCE_ID.NEXTVAL`；
> ② 若用 MyBatis-Plus 写数据，实体 `@TableId` 默认雪花算法自动生成，无需序列。

## 抽取源过程 DDL（Oracle）——供 ETL 抽取工具调用

**与现有 `SP_GetIndicator_24/25/26/27` 的区别**：那些过程返回的是**最终统计结果**；下面这些过程返回的是**最基础的原始元数据**（按日期原样返回源表明细，不做任何统计/聚合），由 ETL 抽取工具调用后搬到目标库。

执行位置：**源库（HIS）**，入参 `p_date` 为抽取日（字符串 `YYYY-MM-DD`），通过 `a_Resultset`（`types_auto.cursorType`）返回结果集，**无需 dblink**。

| 抽取源过程 | 返回内容 | 写入目标表 |
|---|---|---|
| `SP_ETL_GET_OUTP_RCPT` | RCPT_NO / PATIENT_ID / VISIT_DATE / TOTAL_CHARGES / REFUNDED_RCPT_NO / OPERATOR_NO / BILL_CLASS | ETL_OUTP_RCPT |
| `SP_ETL_GET_OUTP_ACCT` | ACCT_DATE / TOTAL_COSTS / REFUND_AMOUNT / RCPTS_NUM / REFUND_NUM | ETL_OUTP_ACCT |
| `SP_ETL_GET_CLINIC` | PATIENT_ID / VISIT_DATE / RETURNED_DATE / REGIST_FEE / CLINIC_FEE / CLINIC_LABEL / VISIT_TIME_DESC / OPERATOR_NO | ETL_CLINIC |
| `SP_ETL_GET_OUTP_PAYMENT` | RCPT_NO / PATIENT_ID / MONEY_TYPE / PAYMENT_AMOUNT / REFUNDED_AMOUNT / VISIT_DATE（关联收据表补日期/患者） | ETL_OUTP_PAYMENT |
| `SP_ETL_GET_TIMESCHEDULE_QUEUE` | SCHEDULE_ID / PATIENT_ID / QUEUE_TYPE / IS_USED / VISIT_DATE（关联挂号表补日期） | ETL_TIMESCHEDULE_QUEUE |

> **ID / CREATE_TIME / UPDATE_TIME 不返回**：由目标侧写入时生成（共享序列 `SEQ_ETL_FINANCE_ID` 或 MyBatis-Plus 雪花算法）。目标侧按「先清当日再插入」保证幂等可重跑。

```sql
-- ============================================================
-- 1. 门诊收据明细（← outp_rcpt_master）→ ETL_OUTP_RCPT
-- ============================================================
CREATE OR REPLACE PROCEDURE SP_ETL_GET_OUTP_RCPT(
    p_date       in varchar2,               -- 抽取日期 YYYY-MM-DD
    a_Resultset  out types_auto.cursorType, -- 返回的结果集
    a_RetCode    out integer,               -- 返回值 1成功 -1失败
    a_ErrMsg     out varchar2               -- 出错信息
) as
begin
    open a_Resultset for
        SELECT RCPT_NO, PATIENT_ID, VISIT_DATE, TOTAL_CHARGES,
               REFUNDED_RCPT_NO, OPERATOR_NO, BILL_CLASS
          FROM outp_rcpt_master
         WHERE VISIT_DATE >= TO_DATE(p_date, 'YYYY-MM-DD')
           AND VISIT_DATE <  TO_DATE(p_date, 'YYYY-MM-DD') + 1;
    a_RetCode := 1;
exception
    when others then
        a_RetCode := -1;
        a_ErrMsg  := SQLERRM;
end SP_ETL_GET_OUTP_RCPT;
/

-- ============================================================
-- 2. 门诊结账明细（← outp_acct_master）→ ETL_OUTP_ACCT
--    收据张数/金额唯一口径
-- ============================================================
CREATE OR REPLACE PROCEDURE SP_ETL_GET_OUTP_ACCT(
    p_date       in varchar2,
    a_Resultset  out types_auto.cursorType,
    a_RetCode    out integer,
    a_ErrMsg     out varchar2
) as
begin
    open a_Resultset for
        SELECT ACCT_DATE, TOTAL_COSTS, REFUND_AMOUNT, RCPTS_NUM, REFUND_NUM
          FROM outp_acct_master
         WHERE ACCT_DATE >= TO_DATE(p_date, 'YYYY-MM-DD')
           AND ACCT_DATE <  TO_DATE(p_date, 'YYYY-MM-DD') + 1;
    a_RetCode := 1;
exception
    when others then
        a_RetCode := -1;
        a_ErrMsg  := SQLERRM;
end SP_ETL_GET_OUTP_ACCT;
/

-- ============================================================
-- 3. 门诊挂号明细（← clinic_master）→ ETL_CLINIC
-- ============================================================
CREATE OR REPLACE PROCEDURE SP_ETL_GET_CLINIC(
    p_date       in varchar2,
    a_Resultset  out types_auto.cursorType,
    a_RetCode    out integer,
    a_ErrMsg     out varchar2
) as
begin
    open a_Resultset for
        SELECT PATIENT_ID, VISIT_DATE, RETURNED_DATE, REGIST_FEE, CLINIC_FEE,
               CLINIC_LABEL, VISIT_TIME_DESC, OPERATOR_NO
          FROM clinic_master
         WHERE VISIT_DATE >= TO_DATE(p_date, 'YYYY-MM-DD')
           AND VISIT_DATE <  TO_DATE(p_date, 'YYYY-MM-DD') + 1;
    a_RetCode := 1;
exception
    when others then
        a_RetCode := -1;
        a_ErrMsg  := SQLERRM;
end SP_ETL_GET_CLINIC;
/

-- ============================================================
-- 4. 门诊支付明细（← outp_payments_money）→ ETL_OUTP_PAYMENT
--    源表无日期列，关联 outp_rcpt_master 取 PATIENT_ID / VISIT_DATE
-- ============================================================
CREATE OR REPLACE PROCEDURE SP_ETL_GET_OUTP_PAYMENT(
    p_date       in varchar2,
    a_Resultset  out types_auto.cursorType,
    a_RetCode    out integer,
    a_ErrMsg     out varchar2
) as
begin
    open a_Resultset for
        SELECT p.RCPT_NO, r.PATIENT_ID, p.MONEY_TYPE,
               p.PAYMENT_AMOUNT, p.REFUNDED_AMOUNT, r.VISIT_DATE
          FROM outp_payments_money p
          JOIN outp_rcpt_master r
            ON r.RCPT_NO = p.RCPT_NO
         WHERE r.VISIT_DATE >= TO_DATE(p_date, 'YYYY-MM-DD')
           AND r.VISIT_DATE <  TO_DATE(p_date, 'YYYY-MM-DD') + 1;
    a_RetCode := 1;
exception
    when others then
        a_RetCode := -1;
        a_ErrMsg  := SQLERRM;
end SP_ETL_GET_OUTP_PAYMENT;
/

-- ============================================================
-- 5. 排班队列（← cq_card.mop_timeschedule_queue）→ ETL_TIMESCHEDULE_QUEUE
--    源表无日期列，关联 clinic_master（schedule_id 拼接 + patient_id）取 VISIT_DATE
-- ============================================================
CREATE OR REPLACE PROCEDURE SP_ETL_GET_TIMESCHEDULE_QUEUE(
    p_date       in varchar2,
    a_Resultset  out types_auto.cursorType,
    a_RetCode    out integer,
    a_ErrMsg     out varchar2
) as
begin
    open a_Resultset for
        SELECT q.SCHEDULE_ID, q.PATIENT_ID, q.QUEUE_TYPE, q.IS_USED,
               c.VISIT_DATE
          FROM cq_card.mop_timeschedule_queue q
          JOIN clinic_master c
            ON c.CLINIC_LABEL || '|' || TO_CHAR(c.VISIT_DATE, 'YYYYMMDD') || '|' || c.VISIT_TIME_DESC = q.SCHEDULE_ID
           AND c.PATIENT_ID = q.PATIENT_ID
         WHERE c.VISIT_DATE >= TO_DATE(p_date, 'YYYY-MM-DD')
           AND c.VISIT_DATE <  TO_DATE(p_date, 'YYYY-MM-DD') + 1;
    a_RetCode := 1;
exception
    when others then
        a_RetCode := -1;
        a_ErrMsg  := SQLERRM;
end SP_ETL_GET_TIMESCHEDULE_QUEUE;
/
```
