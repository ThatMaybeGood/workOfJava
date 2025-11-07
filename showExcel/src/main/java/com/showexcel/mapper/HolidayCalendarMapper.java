package com.showexcel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.showexcel.model.HolidayCalendar;
import com.showexcel.model.HolidayType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2025/10/30 10:58
 */


@Mapper
public interface HolidayCalendarMapper extends BaseMapper<HolidayCalendar> {

    // 自定义查询：根据年份和类型查询
    @Select("SELECT * FROM holiday_calendar WHERE year = #{year} AND holiday_type = #{type}")
    List<HolidayCalendar> selectByYearAndType(@Param("year") Integer year,
                                              @Param("type") String type);

    // 自定义更新：批量更新节假日状态
    @Update("UPDATE holiday_calendar SET is_holiday = #{isHoliday} WHERE id IN (#{ids})")
    int batchUpdateStatus(@Param("ids") List<Long> ids,
                          @Param("isHoliday") Boolean isHoliday);


    // 复杂统计查询
    List<Map<String, Object>> countHolidaysByYear(@Param("startYear") Integer startYear,
                                                  @Param("endYear") Integer endYear);

    // 批量插入
    int batchInsert(@Param("list") List<HolidayCalendar> holidays);


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


    /**
     * 通过日期查询节假日信息
     */
    HolidayCalendar getHolidayCalendarByDate(String date);

    /**
     * 获取指定年份的所有节假日日期
     */
    List<String>  getHolidayDatesByYear(@Param("year") Integer year);

}