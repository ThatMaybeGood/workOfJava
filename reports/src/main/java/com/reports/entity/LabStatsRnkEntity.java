package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 检验统计-排行
 */
@Data
@TableName("TR_LABSTAT_RNK")
public class LabStatsRnkEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId("id")
    private Long id;

    /** 统计日期 */
    @TableField("stat_date")
    private Date statDate;

    /** 排行类型(BLOOD/LAB) */
    @TableField("rank_type")
    private String rankType;

    /** 排名 */
    @TableField("rank_num")
    private Integer rankNum;

    /** 项目名称 */
    @TableField("item_name")
    private String itemName;

    /** 项目值 */
    @TableField("item_value")
    private Integer itemValue;

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
