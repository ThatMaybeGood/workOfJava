package com.showexcel.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2025/10/30 10:46
 */
// HolidayCalendar.java
@Data
@TableName("holiday_calendar")
public class HolidayCalendar {
    @TableId(type = IdType.AUTO)
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date holidayDate;

    private String holidayName;

    private Boolean isHoliday;

    private String holidayType;

    private Integer year;

    private String description;

    private Date createdTime;

    private Date updatedTime;
}

