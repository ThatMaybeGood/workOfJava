package com.mergedata.mapper;

import com.mergedata.model.YQHolidayCalendar;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface HolidayMapper {

    /*
     * 查询所有节假日
     */
    List<YQHolidayCalendar> selectAll();

    /*
     * 根据日期查询节假日信息
     * @Param 指定名称，与 XML 中的 #{holiday_date} 匹配
     */
    List<YQHolidayCalendar> selectByDate(@Param("holiday_date") LocalDate date);

    /*
     * 插入单条节假日信息
     * @return 影响的行数
     */
    int insert(YQHolidayCalendar holiday); // 修正：改为 insert，返回 int

    /*
     * 批量插入节假日列表
     * @return 影响的行数
     */
    int batchInsertList(List<YQHolidayCalendar> holidays); // 修正：返回 int

    /*
     * 更新节假日信息
     * @return 影响的行数
     */
    int delete(String  serialNo); // 修正：返回 int

    int update(String  serialNo); // 修正：返回 int

}