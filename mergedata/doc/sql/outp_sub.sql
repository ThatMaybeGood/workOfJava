-- =============================================================
-- 门诊现金统计子表 mpp_cash_statistics_sub 累积式变更手册
-- 每次变更在末尾追加带日期版本块，幂等可重复执行，不改旧块
-- =============================================================


-- -------------------------------------------------------------
-- v20261029 新增cell_formula列：存储新版前端录入的Excel公式文本
--           JSON串 {字段名: "=公式"}，旧版前端不传此字段(为NULL)
-- -------------------------------------------------------------
DECLARE
    v_cnt NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_cnt FROM user_tab_columns
    WHERE table_name = 'MPP_CASH_STATISTICS_SUB' AND column_name = 'CELL_FORMULA';
    IF v_cnt = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE mpp_cash_statistics_sub ADD cell_formula VARCHAR2(1000)';
    END IF;
END;
/

COMMENT ON COLUMN mpp_cash_statistics_sub.cell_formula IS '前端Excel公式文本JSON {字段名:公式}，新版前端写入，旧版为NULL';

-- 验证
SELECT column_name, data_type, data_length
FROM user_tab_columns
WHERE table_name = 'MPP_CASH_STATISTICS_SUB' AND column_name = 'CELL_FORMULA';
