package com.showexcel.server;

import com.showexcel.model.HolidayCalendar;
import com.showexcel.model.HolidayType;

import java.util.Date;
import java.util.List;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2025/10/30 10:48
 */
public interface HolidayService {

    /**
     * 获取指定年份的节假日日历
     */
    List<HolidayCalendar> getHolidayCalendar(Integer year);

    /**
     * 批量更新节假日信息
     */
    Boolean batchUpdateHolidays(List<HolidayCalendar> holidays);

    /**
     * 标记某天为节假日或工作日
     */
    Boolean markHoliday(Date date, String holidayName, Boolean isHoliday, String holidayType);

    /**
     * 初始化国家法定节假日
     */
    Boolean initNationalHolidays(Integer year);

    /**
     * 获取节假日类型列表
     */
    List<HolidayType> getHolidayTypes();
}