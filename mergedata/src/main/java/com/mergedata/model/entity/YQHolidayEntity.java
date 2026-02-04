package com.mergedata.model.entity;

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
public class YQHolidayEntity {

    private String serialNo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "节假日日期不能为空")
    private LocalDate holidayDate;

    private String validStatus;

    // 节假日类型，周末，节假日，工作日 0，1，2
    private String holidayType;

     private String holidayYear;

    private String holidayMonth;

    private String creator;

    private LocalDateTime createdTime;

    private LocalDate updateTime;

    private String updateCount;

    private String remark;

    private String category; // 节假日分类 门诊/住院/通用 0/1/2
}