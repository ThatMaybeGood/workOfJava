

-- ============================================================
-- 2026-10-15 追加: bt1订单来源慢点修复——MOP_QUEUE.VISIT_NO 类型对齐
-- ★ 已作废(方向反转): 改为 CLINIC_MASTER.VISIT_NO→NUMBER, 见下方「追加(二)」。本块仅保留检查语句供查阅, 勿执行其中 ALTER
-- 背景: EXPLAIN 显示 Q.VISIT_NO = TO_NUMBER(C.VISIT_NO), 两表 VISIT_NO 类型不一致
--       (MOP_QUEUE为NUMBER, CLINIC_MASTER为VARCHAR2, DDL定义均为VARCHAR2(64)),
--       隐式转换使连接列索引失效 → HASH JOIN+两表全表扫, 月跨度bt1约2.2s
-- ============================================================

-- 检查1: 确认两表 VISIT_NO 实际类型（应显示 MOP_QUEUE=NUMBER, CLINIC_MASTER=VARCHAR2）
SELECT table_name, data_type, data_length
FROM user_tab_columns
WHERE table_name IN ('TR_OUTP_FIN_CLINIC_MASTER','TR_OUTP_FIN_MOP_QUEUE') AND column_name = 'VISIT_NO'
ORDER BY table_name;

-- 检查2: CLINIC_MASTER 有无前导零票号（>0 说明当前数值相等语义在对齐后会变化, 先停下来把结果发开发, 勿继续）
SELECT COUNT(*) AS leading_zero_visits
FROM TR_OUTP_FIN_CLINIC_MASTER
WHERE REGEXP_LIKE(VISIT_NO, '^0[0-9]+$');

-- [作废保留] 修复: MOP_QUEUE.VISIT_NO 对齐为 VARCHAR2(64)(方向已反转为追加(二), 勿执行)
-- DECLARE v_type VARCHAR2(30);
-- BEGIN
--   SELECT data_type INTO v_type FROM user_tab_columns
--    WHERE table_name='TR_OUTP_FIN_MOP_QUEUE' AND column_name='VISIT_NO';
--   IF v_type <> 'VARCHAR2' THEN
--     EXECUTE IMMEDIATE 'ALTER TABLE TR_OUTP_FIN_MOP_QUEUE MODIFY (VISIT_NO VARCHAR2(64))';
--   END IF;
-- END;
-- /

-- 修复后: 重收两表统计信息并复核执行计划(应出现 INDEX RANGE SCAN / NESTED LOOP, 不再有全表扫)
BEGIN
  DBMS_STATS.GATHER_TABLE_STATS(ownname=>USER, tabname=>'TR_OUTP_FIN_MOP_QUEUE');
  DBMS_STATS.GATHER_TABLE_STATS(ownname=>USER, tabname=>'TR_OUTP_FIN_CLINIC_MASTER');
END;
/
EXPLAIN PLAN FOR
SELECT nvl(q.SOURCE_NAME, decode(c.operator_no,'9101','互联网订单','当日挂号')) AS name, COUNT(1) AS cnt
FROM TR_OUTP_FIN_CLINIC_MASTER c
LEFT JOIN TR_OUTP_FIN_MOP_QUEUE q
  ON q.STAT_DATE = c.STAT_DATE AND q.visit_no = c.visit_no AND q.APPOINTS_INDICATOR = 2
WHERE c.STAT_DATE BETWEEN DATE '2026-10-01' AND DATE '2026-10-31'
  AND c.APPOINTS_INDICATOR = 2 AND c.IS_RETURN_TYPE = 1
GROUP BY nvl(q.SOURCE_NAME, decode(c.operator_no,'9101','互联网订单','当日挂号'));
SELECT * FROM TABLE(dbms_xplan.display);


-- ============================================================
-- 2026-10-15 追加(二): 类型对齐方向定为 CLINIC_MASTER.VISIT_NO 改 NUMBER(与MOP_QUEUE一致)
-- 背景: 现有连接=TO_NUMBER(C.VISIT_NO)=Q.VISIT_NO 数值相等语义, 本方向对齐后结果与现在完全一致
-- 注意: docs/ddl/19_门诊财务.sql 基线仍写 VARCHAR2(64)——测试环境造数可能有字母前缀就诊号,
--       生产按本块对齐即可, 两类型差异仅在此注释说明
-- ★ 第2步 ALTER 若报 ORA-01439(列非空不可直接改类型), 跳过本块后续, 改执行下方「追加(三)」新列迁移法
-- ============================================================

-- 检查: 不可转数值的就诊号数量, 必须为 0 才能改（>0 先发开发, 勿继续）
SELECT COUNT(*) AS non_numeric_visits
FROM TR_OUTP_FIN_CLINIC_MASTER
WHERE VISIT_NO IS NOT NULL AND NOT REGEXP_LIKE(VISIT_NO, '^[0-9]+$');

-- 修复: VARCHAR2 → NUMBER(18)。若报主键相关错误(ORA-02429/02430等), 改用下方备选三段式
ALTER TABLE TR_OUTP_FIN_CLINIC_MASTER MODIFY (VISIT_NO NUMBER(18));

-- 备选(仅当上一步报主键错误时执行):
-- ALTER TABLE TR_OUTP_FIN_CLINIC_MASTER DROP CONSTRAINT PK_TR_OUTP_FIN_CLINIC_MASTER;
-- ALTER TABLE TR_OUTP_FIN_CLINIC_MASTER MODIFY (VISIT_NO NUMBER(18));
-- ALTER TABLE TR_OUTP_FIN_CLINIC_MASTER ADD CONSTRAINT PK_TR_OUTP_FIN_CLINIC_MASTER PRIMARY KEY (STAT_DATE, VISIT_NO, APPOINTS_INDICATOR);

-- 验证: 重收统计 + 复核 bt1 执行计划(应出现 INDEX RANGE SCAN/NESTED LOOP, 月跨度目标 <1s)
BEGIN
  DBMS_STATS.GATHER_TABLE_STATS(ownname=>USER, tabname=>'TR_OUTP_FIN_CLINIC_MASTER');
END;
/
EXPLAIN PLAN FOR
SELECT nvl(q.SOURCE_NAME, decode(c.operator_no,'9101','互联网订单','当日挂号')) AS name, COUNT(1) AS cnt
FROM TR_OUTP_FIN_CLINIC_MASTER c
LEFT JOIN TR_OUTP_FIN_MOP_QUEUE q
  ON q.STAT_DATE = c.STAT_DATE AND q.visit_no = c.visit_no AND q.APPOINTS_INDICATOR = 2
WHERE c.STAT_DATE BETWEEN DATE '2026-10-01' AND DATE '2026-10-31'
  AND c.APPOINTS_INDICATOR = 2 AND c.IS_RETURN_TYPE = 1
GROUP BY nvl(q.SOURCE_NAME, decode(c.operator_no,'9101','互联网订单','当日挂号'));
SELECT * FROM TABLE(dbms_xplan.display);


-- ============================================================
-- 2026-10-15 追加(三): ORA-01439(列非空不可直接改类型) → 新列迁移法
-- 适用: 追加(二)第2步 ALTER 报 ORA-01439 时按本块执行; 600万行约几分钟, 建议没人查报表时做
-- ============================================================

-- 前置确认仍是 VARCHAR2(已是 NUMBER 则整段跳过)
SELECT data_type FROM user_tab_columns
WHERE table_name='TR_OUTP_FIN_CLINIC_MASTER' AND column_name='VISIT_NO';

-- 1. 删主键(动态找约束名, 兼容ETL自建表的命名)
DECLARE v_pk VARCHAR2(128);
BEGIN
  SELECT constraint_name INTO v_pk FROM user_constraints
   WHERE table_name='TR_OUTP_FIN_CLINIC_MASTER' AND constraint_type='P';
  EXECUTE IMMEDIATE 'ALTER TABLE TR_OUTP_FIN_CLINIC_MASTER DROP CONSTRAINT ' || v_pk;
END;
/

-- 2. 加新列并迁移数据(全表UPDATE约1-2分钟)
ALTER TABLE TR_OUTP_FIN_CLINIC_MASTER ADD (VISIT_NO_NEW NUMBER(18));
UPDATE TR_OUTP_FIN_CLINIC_MASTER SET VISIT_NO_NEW = TO_NUMBER(VISIT_NO);
-- 迁移核对(应等于表总行数, 不等说明有脏数据, 停下来发开发)
-- SELECT COUNT(*) AS migrated, (SELECT COUNT(*) FROM TR_OUTP_FIN_CLINIC_MASTER) AS total
-- FROM TR_OUTP_FIN_CLINIC_MASTER WHERE VISIT_NO_NEW IS NOT NULL;

-- 3. 删旧列、新列改名
ALTER TABLE TR_OUTP_FIN_CLINIC_MASTER DROP COLUMN VISIT_NO;
ALTER TABLE TR_OUTP_FIN_CLINIC_MASTER RENAME COLUMN VISIT_NO_NEW TO VISIT_NO;

-- 4. 加回主键
ALTER TABLE TR_OUTP_FIN_CLINIC_MASTER ADD CONSTRAINT PK_TR_OUTP_FIN_CLINIC_MASTER
  PRIMARY KEY (STAT_DATE, VISIT_NO, APPOINTS_INDICATOR);

-- 5. 重收统计 + 复射手册上方的 bt1 EXPLAIN PLAN 验证
BEGIN
  DBMS_STATS.GATHER_TABLE_STATS(ownname=>USER, tabname=>'TR_OUTP_FIN_CLINIC_MASTER');
END;
/
