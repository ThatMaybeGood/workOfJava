package com.example.messagedataservice.server;

import com.example.messagedataservice.model.HolidayCalendar;

import java.time.LocalDate;
import java.util.List;

public interface HolidayCalendarService {
    /*
     * 根据日期查询节假日信息
     */
    List<HolidayCalendar> findByDate(LocalDate date);
}
