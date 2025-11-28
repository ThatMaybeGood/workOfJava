package com.mergedata.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;


/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @LocalDate 2025/10/30 10:46
 */
// HolidayCalendar.java
@Data
//@TableName("holiday_calendar")
public class YQHolidayCalendarDTO {
//    @TableId(type = IdType.AUTO)
    private String serialNo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate holidayDate;

    private String holidayName;

    private Boolean isValid;

    // 节假日类型，周末，节假日，工作日 0，1，2
    private String holidayType;

    private Integer year;

    private Integer month;


    private String description;

    private String creator;

    private LocalDate createdTime;

    private LocalDate updatedTime;

    private Integer updateCount;

    private String remarks;


}

