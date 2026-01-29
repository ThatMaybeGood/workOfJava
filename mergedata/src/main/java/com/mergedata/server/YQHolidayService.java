package com.mergedata.server;

import com.mergedata.model.dto.HolidayRequestBody;
import com.mergedata.model.entity.YQHolidayCalendarEntity;
import com.mergedata.model.vo.YQHolidayCalendarVO;

import java.time.LocalDate;
import java.util.List;

public interface YQHolidayService {
    /*
     * 根据日期查询节假日信息
     */
    List<YQHolidayCalendarEntity> findAll();

    /*
     * 暂未编写相关过程
     */
    List<YQHolidayCalendarEntity> findByDate(LocalDate date);

    /*
     * 单条写入
     */
    Boolean insert(YQHolidayCalendarEntity holiday);

    /*
     * 批量写入
     */
    Boolean batchInsertList(List<YQHolidayCalendarEntity> holidays);


    /*
     * 作废
     */
    Boolean update(YQHolidayCalendarEntity holiday);

    /*
     * 作废
     */
    Boolean delete(YQHolidayCalendarEntity holiday);


    /*
     * 获取日期对应的类型
     */
    YQHolidayCalendarVO queryDateType(HolidayRequestBody holiday);

    /*
     * 获取日期对应的类型
     */
    String getDateType(LocalDate date);

}
