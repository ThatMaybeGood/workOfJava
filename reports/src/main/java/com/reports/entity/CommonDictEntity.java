package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 通用字典（岗位类别/投诉分类/投诉处理结果/表扬方式/是否反馈科室等）
 */
@Data
@TableName("TR_COMMON_DICT")
public class CommonDictEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId("id")
    private Long id;

    /** 字典类型 */
    @TableField("dict_type")
    private String dictType;

    /** 字典编码 */
    @TableField("dict_code")
    private String dictCode;

    /** 字典名称 */
    @TableField("dict_name")
    private String dictName;

    /** 排序号 */
    @TableField("sort_no")
    private Integer sortNo;

    /** 状态：1启用 0停用 */
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
