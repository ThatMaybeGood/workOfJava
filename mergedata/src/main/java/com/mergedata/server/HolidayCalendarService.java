package com.mergedata.server;

import com.mergedata.dto.HolidayCalendarDTO;

import java.time.LocalDate;
import java.util.List;

public interface HolidayCalendarService {
    /*
     * 根据日期查询节假日信息
     */
    List<HolidayCalendarDTO> findByDate(LocalDate date);
}
