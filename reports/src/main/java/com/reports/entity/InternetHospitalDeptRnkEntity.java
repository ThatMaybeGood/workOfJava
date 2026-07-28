package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 互医质控运营月报-科室排行
 */
@Data
@TableName("TR_INET_HOSP_DEPT_RNK")
public class InternetHospitalDeptRnkEntity implements Serializable {

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
     * 排名
     */
    @TableField("rank_num")
    private Integer rankNum;

    /**
     * 科室名称
     */
    @TableField("dept_name")
    private String deptName;

    /**
     * 当月值
     */
    @TableField("current_month")
    private Integer currentMonth;

    /**
     * 上月值
     */
    @TableField("last_month")
    private Integer lastMonth;

    /**
     * 增长率
     */
    @TableField("growth_rate")
    private String growthRate;

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
