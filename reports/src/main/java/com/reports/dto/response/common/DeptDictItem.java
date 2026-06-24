package com.reports.dto.response.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 科室字典项
 */
@Data
public class DeptDictItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 科室编码
     */
    private String deptCode;

    /**
     * 科室名称
     */
     private String deptName;

    /**
     * 科室类型：0 门诊，1 住院，2 其他
     */
    private Integer deptType;

    /**
     * 父科室编码
     */
    private String parentCode;

    /**
     * 科室层级
     */
    private Integer level;

    /**
     * 排序号
     */
    private Integer sortNo;

    /**
     * 是否有效：0 无效，1 有效
     */
    private Integer status;
}
