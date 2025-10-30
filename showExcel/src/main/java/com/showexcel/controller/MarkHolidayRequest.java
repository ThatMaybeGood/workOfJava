package com.showexcel.controller;

import lombok.Data;

import java.util.Date;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2025/10/30 11:01
 */ // 请求DTO
@Data
class MarkHolidayRequest {
    private Date date;
    private String holidayName;
    private Boolean isHoliday;
    private String holidayType;
}
