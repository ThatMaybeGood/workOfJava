-- ============================================================
-- 公共对象
-- ============================================================

-- 主键序列
CREATE SEQUENCE seq_tr_reports START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- 科室字典表(全院公共,各报表科室下拉/过滤共用)
-- ============================================================
CREATE TABLE TR_DEPT_DICT (
    serial_no             NUMBER(6),                       -- 排序号
    dept_code             VARCHAR2(50)  NOT NULL,          -- 科室编码
    dept_name             VARCHAR2(100) NOT NULL,          -- 科室名称
    dept_alias            VARCHAR2(100),                   -- 科室别名
    clinic_attr           VARCHAR2(10),                    -- 临床属性
    outp_or_inp           VARCHAR2(10),                    -- 门诊住院标志(0门诊/1住院/2两者/9其他)
    internal_or_sergery   VARCHAR2(10),                    -- 内外科标志(0内科)
    input_code            VARCHAR2(50),                    -- 输入码
    type_code             VARCHAR2(10),                    -- 科室类型
    group_unit_name       VARCHAR2(100)                    -- 所属单元
);
CREATE UNIQUE INDEX uk_tr_dept_dict_code ON TR_DEPT_DICT(dept_code);

COMMENT ON TABLE TR_DEPT_DICT IS '科室字典表(全院公共字典,各报表科室下拉/过滤共用)';
COMMENT ON COLUMN TR_DEPT_DICT.serial_no IS '排序号';
COMMENT ON COLUMN TR_DEPT_DICT.dept_code IS '科室编码';
COMMENT ON COLUMN TR_DEPT_DICT.dept_name IS '科室名称';
COMMENT ON COLUMN TR_DEPT_DICT.dept_alias IS '科室别名';
COMMENT ON COLUMN TR_DEPT_DICT.clinic_attr IS '临床属性';
COMMENT ON COLUMN TR_DEPT_DICT.outp_or_inp IS '门诊住院标志(0门诊/1住院/2两者/9其他)';
COMMENT ON COLUMN TR_DEPT_DICT.internal_or_sergery IS '内外科标志(0内科)';
COMMENT ON COLUMN TR_DEPT_DICT.input_code IS '输入码';
COMMENT ON COLUMN TR_DEPT_DICT.type_code IS '科室类型';
COMMENT ON COLUMN TR_DEPT_DICT.group_unit_name IS '所属单元';
