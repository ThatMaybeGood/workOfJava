package com.mergedata.server;

import com.mergedata.model.YQHolidayCalendar;

import java.util.List;

public interface YQHolidayService {
    /*
     * 根据日期查询节假日信息
     */
    List<YQHolidayCalendar> findAll();

    /*
     * 暂未编写相关过程
     */
    List<YQHolidayCalendar> findByDate(String date);

    /*
     * 单条写入
     */
    Boolean batchInsert(YQHolidayCalendar holiday);

    /*
     * 批量写入
     */
    Boolean batchInsertList(List<YQHolidayCalendar> holidays);


    /*
     * 作废
     */
    Boolean update(YQHolidayCalendar holiday);


}
