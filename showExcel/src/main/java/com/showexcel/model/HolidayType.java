package com.showexcel.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2025/10/30 10:47
 */
// HolidayType.java
@Data
@TableName("holiday_type")
public class HolidayType {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String typeCode;

    private String typeName;

    private String color;

    private Boolean isSystem;

    private Date createdTime;
}