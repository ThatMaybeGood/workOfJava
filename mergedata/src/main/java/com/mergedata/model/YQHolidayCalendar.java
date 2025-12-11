package com.mergedata.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @LocalDate 2025/10/30 10:46
 */
@Data
public class YQHolidayCalendar {

    // 默认不需要指定 JDBC Type，MyBatis-Plus/MyBatis 可以推断 VARCHAR
    private String serialNo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "节假日日期不能为空")
    private LocalDate holidayDate;

    // 增加 @TableField 显式指定 JDBC Type，防止 Oracle 插入 NULL 时报错
    private String validStatus;

    // 节假日类型，周末，节假日，工作日 0，1，2
    // 增加 @TableField 显式指定 JDBC Type，防止 Oracle 插入 NULL 时报错
    private String holidayType;

    // Integer 类型的字段，如果使用 MyBatis-Plus 自动生成 SQL，通常不需要 Type 指定
    private String holidayYear;

    private String holidayMonth;

    private String creator;

    private LocalDateTime createdTime;

    private LocalDate updateTime;

    private String updateCount;

    // 增加 @TableField 显式指定 JDBC Type，防止 Oracle 插入 NULL 时报错
    private String remark;
}