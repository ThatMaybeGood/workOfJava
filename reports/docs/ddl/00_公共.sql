-- ============================================================
-- 公共对象
-- ============================================================

-- 清理已存在对象(如重建请先执行)

DROP SEQUENCE seq_tr_reports;

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
    clinic_attr           NUMBER(2),                       -- 临床属性(0临床/1辅诊/2护理单元/3机关/9其他)
    outp_or_inp           NUMBER(2),                       -- 门诊住院标志(0门诊/1住院/2两者/9其他)
    internal_or_sergery   NUMBER(2),                       -- 内外科标志(0内科/1外科)
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
COMMENT ON COLUMN TR_DEPT_DICT.clinic_attr IS '临床属性(0临床/1辅诊/2护理单元/3机关/9其他)';
COMMENT ON COLUMN TR_DEPT_DICT.outp_or_inp IS '门诊住院标志(0门诊/1住院/2两者/9其他)';
COMMENT ON COLUMN TR_DEPT_DICT.internal_or_sergery IS '内外科标志(0内科/1外科)';
COMMENT ON COLUMN TR_DEPT_DICT.input_code IS '输入码';
COMMENT ON COLUMN TR_DEPT_DICT.type_code IS '科室类型';
COMMENT ON COLUMN TR_DEPT_DICT.group_unit_name IS '所属单元';

-- 通用字典表（岗位类别/投诉分类/投诉处理结果/表扬方式/是否反馈科室等）
CREATE TABLE tr_common_dict (
    id          NUMBER(19)      PRIMARY KEY,
    dict_type   VARCHAR2(50)    NOT NULL,           -- 字典类型：position/complaintCategory/complaintResult/praiseMethod/feedback
    dict_code   VARCHAR2(50)    NOT NULL,           -- 字典编码
    dict_name   VARCHAR2(200)   NOT NULL,           -- 字典名称
    sort_no     NUMBER(6)       DEFAULT 0,          -- 排序号
    status      NUMBER(1)       DEFAULT 1,          -- 状态：1启用 0停用
    create_time DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1        VARCHAR2(500),                        -- 扩展字段1
    ext2        VARCHAR2(500),                        -- 扩展字段2
    ext3        VARCHAR2(500)                         -- 扩展字段3
);

-- 人员字典表
CREATE TABLE tr_staff_dict (
    id              NUMBER(19)      PRIMARY KEY,
    staff_code      VARCHAR2(50)    NOT NULL,           -- 人员工号(源user_id)
    staff_name      VARCHAR2(100)   NOT NULL,           -- 人员姓名
    login_account   VARCHAR2(50),                       -- 登录账号(源表主键db_user)
    xq_id           NUMBER(5),                          -- 本院数字ID
    dept_code       VARCHAR2(10),                       -- 所属科室代码（关联 TR_DEPT_DICT）
    dept_name       VARCHAR2(100),                      -- 所属科室名称
    position        VARCHAR2(50),                       -- 岗位类别
    status          NUMBER(1)       DEFAULT 1,          -- 状态：1在职 0停用
    create_time     DATE            DEFAULT SYSDATE,       -- 创建时间
    update_time     DATE            DEFAULT SYSDATE,       -- 更新时间
    ext1            VARCHAR2(500),                        -- 扩展字段1
    ext2            VARCHAR2(500),                        -- 扩展字段2
    ext3            VARCHAR2(500)                         -- 扩展字段3
);





COMMENT ON TABLE tr_common_dict IS '通用字典表(岗位类别/投诉分类/投诉处理结果/表扬方式等)';
COMMENT ON COLUMN tr_common_dict.id IS '主键ID';
COMMENT ON COLUMN tr_common_dict.dict_type IS '字典类型：position/complaintCategory/complaintResult/praiseMethod/feedback';
COMMENT ON COLUMN tr_common_dict.dict_code IS '字典编码';
COMMENT ON COLUMN tr_common_dict.dict_name IS '字典名称';
COMMENT ON COLUMN tr_common_dict.sort_no IS '排序号';
COMMENT ON COLUMN tr_common_dict.status IS '状态：1启用 0停用';
COMMENT ON COLUMN tr_common_dict.create_time IS '创建时间';
COMMENT ON COLUMN tr_common_dict.update_time IS '更新时间';
COMMENT ON COLUMN tr_common_dict.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_common_dict.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_common_dict.ext3 IS '扩展字段3';
COMMENT ON TABLE tr_staff_dict IS '人员字典表';
COMMENT ON COLUMN tr_staff_dict.id IS '主键ID';
COMMENT ON COLUMN tr_staff_dict.staff_code IS '人员工号';
COMMENT ON COLUMN tr_staff_dict.staff_name IS '人员姓名';
COMMENT ON COLUMN tr_staff_dict.login_account IS '登录账号(源表主键db_user)';
COMMENT ON COLUMN tr_staff_dict.xq_id IS '本院数字ID';
COMMENT ON COLUMN tr_staff_dict.dept_code IS '所属科室代码';
COMMENT ON COLUMN tr_staff_dict.dept_name IS '所属科室名称';
COMMENT ON COLUMN tr_staff_dict.position IS '岗位类别';
COMMENT ON COLUMN tr_staff_dict.status IS '状态：1在职 0停用';
COMMENT ON COLUMN tr_staff_dict.create_time IS '创建时间';
COMMENT ON COLUMN tr_staff_dict.update_time IS '更新时间';
COMMENT ON COLUMN tr_staff_dict.ext1 IS '扩展字段1';
COMMENT ON COLUMN tr_staff_dict.ext2 IS '扩展字段2';
COMMENT ON COLUMN tr_staff_dict.ext3 IS '扩展字段3';


-- ============================================================
-- 人员字典导入(源:HIS comm.users,CSV 中转,id 走公共序列)
-- ============================================================

-- 第1步 源库(HIS)执行,查询结果右键导出 CSV(编码UTF-8):
-- SELECT u.db_user, u.user_id, NVL(u.full_name, u.user_name) AS staff_name,
--        u.user_dept AS dept_code, d.dept_name, u.stop_date, u.user_xq_id AS xq_id
-- FROM comm.users u
-- LEFT JOIN comm.dept_dict d ON d.dept_code = u.user_dept
-- WHERE u.user_id IS NOT NULL;

-- 第2步 建暂存表接住 CSV(SQL Developer 对表右键 Import Data,日期格式YYYY-MM-DD):
CREATE TABLE tmp_staff_import (
    db_user     VARCHAR2(50),
    user_id     VARCHAR2(50),
    staff_name  VARCHAR2(100),
    dept_code   VARCHAR2(10),
    dept_name   VARCHAR2(100),
    stop_date   DATE,
    xq_id       NUMBER(5)
);

-- 第3步 灌入人员字典:
INSERT INTO tr_staff_dict
    (id, staff_code, staff_name, login_account, xq_id, dept_code, dept_name, status, create_time, update_time)
SELECT
    seq_tr_reports.NEXTVAL,
    u.user_id,
    u.staff_name,
    u.db_user,
    u.xq_id,
    u.dept_code,
    u.dept_name,
    CASE WHEN u.stop_date IS NULL THEN 1 ELSE 0 END,
    SYSDATE,
    SYSDATE
FROM tmp_staff_import u
WHERE u.user_id IS NOT NULL;

-- 第4步 清理暂存表:
DROP TABLE tmp_staff_import;
