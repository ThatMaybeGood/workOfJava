package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 互医质控运营月报-增长趋势
 */
@Data
@TableName("TR_INET_HOSP_GRW")
public class InternetHospitalGrwEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId("id")
    private Long id;

    /**
     * 统计月份(YYYY-MM)
     */
    @TableField("stat_month")
    private String statMonth;

    /**
     * 分类(月份)
     */
    @TableField("category")
    private String category;

    /**
     * 数值
     */
    @TableField("data_value")
    private Integer dataValue;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;

    /**
     * 扩展字段1
     */
    @TableField("ext1")
    private String ext1;

    /**
     * 扩展字段2
     */
    @TableField("ext2")
    private String ext2;

    /**
     * 扩展字段3
     */
    @TableField("ext3")
    private String ext3;
}
