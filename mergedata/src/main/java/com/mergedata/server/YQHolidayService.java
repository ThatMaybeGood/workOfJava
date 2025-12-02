package com.mergedata.server;

import com.mergedata.model.YQHolidayCalendarDTO;

import java.util.List;

public interface YQHolidayService {
    /*
     * 根据日期查询节假日信息
     */
    List<YQHolidayCalendarDTO> findByDate();


    Boolean insert(List<YQHolidayCalendarDTO> list);
}
