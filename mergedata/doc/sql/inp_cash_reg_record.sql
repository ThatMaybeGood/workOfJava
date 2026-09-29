-- 住院现金登记表（手机端登记库存现金/节假日交款，与 InpCashRegRecordEntity 代码一致）
-- 报表按 save_date + operator_no 匹配取 petty_amount(库存现金18)/difference_amount(节假日交款17)，无登记默认0；差额(19)=(18)-(10)-(17)公式计算
CREATE TABLE MPP_INP_CASH_REG_RECORD
(
  apply_date        DATE DEFAULT TRUNC(SYSDATE),
  operator          VARCHAR2(30),
  ope_type          VARCHAR2(20),
  window_no         VARCHAR2(30),
  scheduling        VARCHAR2(10),
  save_date         DATE NOT NULL,
  petty_amount      NUMBER(10,2),
  difference_amount NUMBER(10,2),
  operator_no       VARCHAR2(30) NOT NULL,
  db_user           VARCHAR2(30),
  ext1              VARCHAR2(30),
  ext2              VARCHAR2(30),
  ext3              VARCHAR2(30),
  create_time       DATE DEFAULT SYSDATE NOT NULL,
  update_time       DATE
)
TABLESPACE POWERMPP;

COMMENT ON COLUMN MPP_INP_CASH_REG_RECORD.apply_date IS '申请日期';
COMMENT ON COLUMN MPP_INP_CASH_REG_RECORD.operator IS '申请人';
COMMENT ON COLUMN MPP_INP_CASH_REG_RECORD.ope_type IS '操作类型 0,1,2 单条写入/批量写入/修改';
COMMENT ON COLUMN MPP_INP_CASH_REG_RECORD.window_no IS '窗口号';
COMMENT ON COLUMN MPP_INP_CASH_REG_RECORD.scheduling IS '班次 (白班 夜班 年假 周末 其他假 1，2，3，4，5)';
COMMENT ON COLUMN MPP_INP_CASH_REG_RECORD.save_date IS '登记日期';
COMMENT ON COLUMN MPP_INP_CASH_REG_RECORD.petty_amount IS '库存现金(报表第18列)';
COMMENT ON COLUMN MPP_INP_CASH_REG_RECORD.difference_amount IS '节假日交款(报表第17列, 列名为difference_amount)';
COMMENT ON COLUMN MPP_INP_CASH_REG_RECORD.operator_no IS '操作员编号';
COMMENT ON COLUMN MPP_INP_CASH_REG_RECORD.db_user IS '登录编号';
COMMENT ON COLUMN MPP_INP_CASH_REG_RECORD.create_time IS '创建时间';

ALTER TABLE MPP_INP_CASH_REG_RECORD
  ADD CONSTRAINT PK_INP_REG_RECORD PRIMARY KEY (SAVE_DATE, OPERATOR_NO)
  USING INDEX TABLESPACE POWERMPP;
