-- ============================================================
-- SP_DISCH_SETTLE_CNT_1116
-- purpose: extract raw settle rows of inp_settle_master (with users name and
--   inp_acct_master invoice count on row, no aggregation) into report-DB
--   tr_disch_settle_cnt_d (column comments see docs/ddl file 25);
--   used when report switch reports.his-mid-table.disch-settle-cnt=true
-- params: p_start_date/p_end_date = 'YYYY-MM-DD', both inclusive; single day
--   = start=end=that day; loader deletes covered stat_date first, then inserts
-- run in HIS (netcen_yb); keep this file ASCII-only (DB is US7ASCII)
-- ============================================================

CREATE OR REPLACE PROCEDURE SP_DISCH_SETTLE_CNT_1116(
    p_start_date in varchar2,               -- range start YYYY-MM-DD (inclusive)
    p_end_date   in varchar2,               -- range end   YYYY-MM-DD (inclusive)
    a_Resultset  out types_auto.cursorType, -- result rows
    a_RetCode    out integer,               -- 1 ok / -1 fail
    a_ErrMsg     out varchar2               -- error message
) as
begin
    open a_Resultset for
        SELECT TRUNC(t.SETTLING_DATE) AS stat_date,
               t.RCPT_NO,
               t.ACCT_NO,
               t.OPERATOR_NO,
               RAWTOHEX(TRIM(t.CHARGE_TYPE)) AS charge_type_hex,
               t.PATIENT_ID,
               t.VISIT_ID,
               t.SETTLING_DATE,
               RAWTOHEX(NVL(u.USER_NAME, t.OPERATOR_NO)) AS operator_name_hex,
               m.INVOICE_PRINTED_TOTAL
          FROM inp_settle_master t
          LEFT JOIN users u
            ON u.USER_ID = t.OPERATOR_NO
          LEFT JOIN inp_acct_master m
            ON m.ACCT_NO = t.ACCT_NO
           AND m.OPERATOR_NO = t.OPERATOR_NO
         WHERE t.SETTLING_DATE >= TO_DATE(p_start_date, 'YYYY-MM-DD')
           AND t.SETTLING_DATE <  TO_DATE(p_end_date, 'YYYY-MM-DD') + 1;
    a_RetCode := 1;
exception
    when others then
        a_RetCode := -1;
        a_ErrMsg  := SQLERRM;
end SP_DISCH_SETTLE_CNT_1116;
/
