package com.mergedata.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.mergedata.util.ValidStatusEnum;
import lombok.Data;

import javax.validation.constraints.NotBlank;
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
     private String serialNo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotBlank(message = "节假日日期不能为空")
    private LocalDate holidayDate;

    private String holidayName;

    private ValidStatusEnum validStatus;

    // 节假日类型，周末，节假日，工作日 0，1，2
    private String holidayType;

    private Integer year;

    private Integer month;


    private String description;

    private String creator;

    private LocalDateTime createdTime;

    private LocalDate updatedTime;

    private Integer updateCount;

    private String remarks;


}

