package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 人员字典
 */
@Data
@TableName("TR_STAFF_DICT")
public class StaffDictEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId("id")
    private Long id;

    /** 人员工号 */
    @TableField("staff_code")
    private String staffCode;

    /** 人员姓名 */
    @TableField("staff_name")
    private String staffName;

    /** 所属科室代码 */
    @TableField("dept_code")
    private String deptCode;

    /** 所属科室名称 */
    @TableField("dept_name")
    private String deptName;

    /** 岗位类别 */
    @TableField("position")
    private String position;

    /** 状态：1在职 0停用 */
    @TableField("status")
    private Integer status;

    /** 创建时间 */
    @TableField("create_time")
    private Date createTime;

    /** 更新时间 */
    @TableField("update_time")
    private Date updateTime;

    /** 扩展字段1 */
    @TableField("ext1")
    private String ext1;

    /** 扩展字段2 */
    @TableField("ext2")
    private String ext2;

    /** 扩展字段3 */
    @TableField("ext3")
    private String ext3;
}
