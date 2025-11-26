package com.mergedata.dto;


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
public class HolidayCalendarDTO {
//    @TableId(type = IdType.AUTO)
    private String serialNo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate holidayDate;

    private String holidayName;

    private Boolean isValid;

    // 节假日类型，周末，节假日，工作日 0，1，2
    private String holidayType;

    private Integer year;

    private String description;

    private String creator;

    private LocalDate createdTime;

    private LocalDate updatedTime;


    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public LocalDate getHolidayDate() {
        return holidayDate;
    }

    public void setHolidayDate(LocalDate holidayDate) {
        this.holidayDate = holidayDate;
    }

    public String getHolidayName() {
        return holidayName;
    }

    public void setHolidayName(String holidayName) {
        this.holidayName = holidayName;
    }

    public Boolean getValid() {
        return isValid;
    }

    public void setValid(Boolean valid) {
        isValid = valid;
    }

    public String getHolidayType() {
        return holidayType;
    }

    public void setHolidayType(String holidayType) {
        this.holidayType = holidayType;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public LocalDate getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDate createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDate getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDate updatedTime) {
        this.updatedTime = updatedTime;
    }
}

