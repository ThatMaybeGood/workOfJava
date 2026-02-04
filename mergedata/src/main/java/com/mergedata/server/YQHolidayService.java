package com.mergedata.server;

import com.mergedata.model.entity.YQHolidayEntity;

import java.time.LocalDate;
import java.util.List;

public interface YQHolidayService {
    /*
     * 根据日期查询节假日信息
     */
    List<YQHolidayEntity> findAll();

    /*
     * 暂未编写相关过程
     */
    List<YQHolidayEntity> findByDate(LocalDate date);

    /*
     * 单条写入
     */
    Boolean insert(YQHolidayEntity holiday);

    /*
     * 批量写入
     */
    Boolean batchInsertList(List<YQHolidayEntity> holidays);


    /*
     * 作废
     */
    Boolean update(YQHolidayEntity holiday);

    /*
     * 作废
     */
    Boolean delete(YQHolidayEntity holiday);


    /*
     * 获取日期对应的类型
     */
    String queryDateType(LocalDate holidayDate,String queryType);

}
