package com.reports.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 每日天气源表
 */
@Data
@TableName("TR_FC_WEATHER")
public class WeatherEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 天气日期 */
    @TableId("weather_date")
    private Date weatherDate;

    /** 天气类型(晴/阴/雨/雪等) */
    @TableField("weather_type")
    private String weatherType;

    /** 天气出勤系数(空按1处理) */
    @TableField("weather_coef")
    private Double weatherCoef;

    /** 来源(人工登记/接口同步) */
    @TableField("weather_source")
    private String weatherSource;

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
