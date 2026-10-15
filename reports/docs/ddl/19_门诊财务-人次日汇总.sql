-- ============================================================
-- 门诊财务报表-人次日汇总 使用手册
-- 模式: 历史人次固定在表里直接查; 当天人次由查询侧实时算(无需维护)。
--       自动任务默认开启(task-enabled=true): 启动时补一次缺口 + 每日cron(默认03:00,
--       须在ETL完成后)滚动重算近7天; 缺失扫描只补"明细有汇总无"的天, 已回填的历史不重复计算。
--       全量重建/手动补数用下方【补全】SQL(幂等, 只算缺失天); 自动关闭时全靠它。
-- ============================================================

-- 【补全】缺失天一次性补全（全量历史约几分钟, 日常只补新的一天时秒级; 可反复执行）
INSERT INTO TR_OUTP_FIN_VISIT_DAY (STAT_DATE, OPERATOR_NO, CNT_ALL, CNT_IN, CNT_REFUND, UPDATE_TIME)
WITH src AS (
    SELECT TRUNC(r.STAT_DATE) AS stat_date,
           NVL(r.OPERATOR_NO, '-') AS OPERATOR_NO,
           r.PATIENT_ID,
           NVL(RTRIM(r.RCPT_NO, '0123456789'), '#') AS prefix,
           TO_NUMBER(SUBSTR(r.RCPT_NO, NVL(LENGTH(RTRIM(r.RCPT_NO, '0123456789')), 0) + 1)) AS num_part,
           NVL(r.TOTAL_CHARGES, 0) AS charges,
           r.REFUNDED_RCPT_NO
    FROM TR_OUTP_FIN_RCPT_ACCT r
    WHERE TRUNC(r.STAT_DATE) < TRUNC(SYSDATE)
      AND TRUNC(r.STAT_DATE) NOT IN (SELECT STAT_DATE FROM TR_OUTP_FIN_VISIT_DAY)
),
all_ranked AS (
    SELECT stat_date, OPERATOR_NO,
        CASE
            WHEN PATIENT_ID IS NOT NULL
             AND num_part = LAG(num_part) OVER (PARTITION BY PATIENT_ID, stat_date, prefix ORDER BY num_part) + 1
            THEN 0 ELSE 1
        END AS flag
    FROM src
),
in_ranked AS (
    SELECT stat_date, OPERATOR_NO,
        CASE
            WHEN PATIENT_ID IS NOT NULL
             AND num_part = LAG(num_part) OVER (PARTITION BY PATIENT_ID, stat_date, prefix ORDER BY num_part) + 1
            THEN 0 ELSE 1
        END AS flag
    FROM src
    WHERE charges >= 0
),
refund_ranked AS (
    SELECT stat_date, OPERATOR_NO,
        CASE
            WHEN PATIENT_ID IS NOT NULL
             AND num_part = LAG(num_part) OVER (PARTITION BY PATIENT_ID, stat_date, prefix ORDER BY num_part) + 1
            THEN 0 ELSE 1
        END AS flag
    FROM src
    WHERE REFUNDED_RCPT_NO IS NOT NULL AND charges < 0
)
SELECT a.stat_date, a.OPERATOR_NO, a.cnt_all, NVL(b.cnt_in, 0), NVL(c.cnt_refund, 0), SYSDATE
FROM (SELECT stat_date, OPERATOR_NO, SUM(flag) AS cnt_all FROM all_ranked GROUP BY stat_date, OPERATOR_NO) a
LEFT JOIN (SELECT stat_date, OPERATOR_NO, SUM(flag) AS cnt_in FROM in_ranked GROUP BY stat_date, OPERATOR_NO) b
    ON b.stat_date = a.stat_date AND b.OPERATOR_NO = a.OPERATOR_NO
LEFT JOIN (SELECT stat_date, OPERATOR_NO, SUM(flag) AS cnt_refund FROM refund_ranked GROUP BY stat_date, OPERATOR_NO) c
    ON c.stat_date = a.stat_date AND c.OPERATOR_NO = a.OPERATOR_NO
ORDER BY a.stat_date;
COMMIT;

-- 【对账】补全后验证：3.1 与 3.2 的 cnt_all 必须相等（日期换成实际存在的）
-- 3.1 实时算法（旧版口径, 约1s）
SELECT SUM(cnt) AS realtime_cnt_all FROM (
    WITH extracted AS (
        SELECT r.PATIENT_ID AS patient_id,
               NVL(RTRIM(r.RCPT_NO, '0123456789'), '#') AS prefix,
               TO_NUMBER(SUBSTR(r.RCPT_NO, NVL(LENGTH(RTRIM(r.RCPT_NO, '0123456789')), 0) + 1)) AS num_part
        FROM TR_OUTP_FIN_RCPT_ACCT r
        WHERE r.STAT_DATE = DATE '2026-10-13'
    ),
    ordered AS (
        SELECT CASE
                   WHEN patient_id IS NOT NULL
                    AND num_part = LAG(num_part) OVER (PARTITION BY patient_id, prefix ORDER BY num_part) + 1
                   THEN 0 ELSE 1 END AS is_new_visit
        FROM extracted
    )
    SELECT is_new_visit AS cnt FROM ordered
);
-- 3.2 汇总表（毫秒级）
SELECT SUM(CNT_ALL) AS cnt_all,
       SUM(CNT_IN)  AS cnt_in,
       SUM(CNT_REFUND) AS cnt_refund
FROM TR_OUTP_FIN_VISIT_DAY
WHERE STAT_DATE = DATE '2026-10-13';

-- 【日常维护】
-- 1. ETL 每天跑完后: 执行一次上方【补全】(NOT IN 只补新的一天, 秒级)
-- 2. ETL 重抽了某天: 先删该天再执行【补全】
--    DELETE FROM TR_OUTP_FIN_VISIT_DAY WHERE STAT_DATE = DATE '2026-xx-xx';
--    COMMIT;
-- 3. 覆盖范围检查: SELECT MIN(STAT_DATE), MAX(STAT_DATE), COUNT(*) FROM TR_OUTP_FIN_VISIT_DAY;
