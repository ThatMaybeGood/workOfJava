package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 科室字典实体
 * 对应表 TR_DEPT_DICT
 */
@Data
@TableName("TR_DEPT_DICT")
public class DeptDictEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 序号
     */
    @TableField(value = "serial_no")
    private Integer serialNo;

    /**
     * 科室代码
     */
    @TableId(value = "dept_code")
    private String deptCode;

    /**
     * 科室名称
     */
    @TableField(value = "dept_name")
    private String deptName;

    /**
     * 科室别名
     */
    @TableField(value = "dept_alias")
    private String deptAlias;

    /**
     * 临床属性：0临床,1辅诊,2护理单元,3机关,9其他
     */
    @TableField(value = "clinic_attr")
    private Integer clinicAttr;

    /**
     * 门诊或住院：0门诊,1住院,2门诊住院,9其他
     */
    @TableField(value = "outp_or_inp")
    private Integer outpOrInp;

    /**
     * 内科或外科：0内科,1外科
     */
    @TableField(value = "internal_or_sergery")
    private Integer internalOrSergery;

    /**
     * 输入码
     */
    @TableField(value = "input_code")
    private String inputCode;

    /**
     * 类型代码
     */
    @TableField(value = "type_code")
    private String typeCode;

    /**
     * 科室组名
     */
    @TableField(value = "group_unit_name")
    private String groupUnitName;
}
