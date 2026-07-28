package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 诊室使用率分析-概览
 */
@Data
@TableName("TR_ROOM_USE_OV")
public class RoomUsageOvEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId("id")
    private Long id;

    /** 统计日期 */
    @TableField("stat_date")
    private Date statDate;

    /** 平均使用率 */
    @TableField("avg_usage")
    private String avgUsage;

    /** 上午使用率 */
    @TableField("am_usage")
    private String amUsage;

    /** 下午使用率 */
    @TableField("pm_usage")
    private String pmUsage;

    /** 节假日使用率 */
    @TableField("holiday_usage")
    private String holidayUsage;

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
