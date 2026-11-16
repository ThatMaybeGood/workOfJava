-- ============================================================
-- SP_PREHOSP_TRANS_1116
-- purpose: extract raw negative-receipt rows of outp_rcpt_master
--   (total_charges<0 and refunded_rcpt_no not null, same filter as the
--   report, no aggregation) into report-DB tr_prehosp_trans_d (column
--   comments see docs/ddl file 22); valid-operator filtering stays on the
--   report side; used when report switch reports.his-mid-table.pre-hosp-trans=true
-- params: p_start_date/p_end_date = 'YYYY-MM-DD', both inclusive; single day
--   = start=end=that day; loader deletes covered stat_date first, then inserts
-- run in HIS (netcen_yb); keep this file ASCII-only (DB is US7ASCII)
-- ============================================================

CREATE OR REPLACE PROCEDURE SP_PREHOSP_TRANS_1116(
    p_start_date in varchar2,               -- range start YYYY-MM-DD (inclusive)
    p_end_date   in varchar2,               -- range end   YYYY-MM-DD (inclusive)
    a_Resultset  out types_auto.cursorType, -- result rows
    a_RetCode    out integer,               -- 1 ok / -1 fail
    a_ErrMsg     out varchar2               -- error message
) as
begin
    open a_Resultset for
        SELECT TRUNC(t.VISIT_DATE) AS stat_date,
               t.RCPT_NO,
               t.VISIT_DATE,
               t.OPERATOR_NO,
               t.PATIENT_ID,
               t.TOTAL_CHARGES,
               t.REFUNDED_RCPT_NO
          FROM outp_rcpt_master t
         WHERE t.VISIT_DATE >= TO_DATE(p_start_date, 'YYYY-MM-DD')
           AND t.VISIT_DATE <  TO_DATE(p_end_date, 'YYYY-MM-DD') + 1
           AND t.TOTAL_CHARGES < 0
           AND t.REFUNDED_RCPT_NO IS NOT NULL;
    a_RetCode := 1;
exception
    when others then
        a_RetCode := -1;
        a_ErrMsg  := SQLERRM;
end SP_PREHOSP_TRANS_1116;
/
