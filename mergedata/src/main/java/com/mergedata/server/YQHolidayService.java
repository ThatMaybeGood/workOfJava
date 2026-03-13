package com.mergedata.server;

import com.mergedata.model.dto.CommonRequestBody;
import com.mergedata.model.entity.YQHolidayEntity;
import com.mergedata.model.vo.YQHolidayCalendarVO;

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
     * 通过年份查询节假日信息
     */
    List<YQHolidayEntity> findByYear(Integer year);

    /*
     * 通过年份+月份查询节假日信息
     */
    List<YQHolidayEntity> findByYearMonth(CommonRequestBody body);


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


    /*
     * 查询节假日是否汇总以及类型
     */
    YQHolidayCalendarVO queryHolidayTotalType(LocalDate holidayDate, String queryType,String totalFlag);



    /**
     * 查找最近的工作日或月初 1 号，作为回溯截止日期
     */
    LocalDate findMinBacktrackDate(LocalDate localDate);


    /**
     * 判断是否符合特殊节假日需要进行回溯汇总计算
     */
    Integer isSpecialHolidaySum(LocalDate localDate,String totalFlag) ;

}
