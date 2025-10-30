package com.showexcel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.showexcel.model.HolidayCalendar;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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

    // 不需要写任何方法，就已经拥有了以下功能：
    // - insert(HolidayCalendar entity)       → 插入数据
    // - deleteById(Serializable id)         → 根据ID删除
    // - updateById(HolidayCalendar entity)  → 根据ID更新
    // - selectById(Serializable id)         → 根据ID查询
    // - selectList(Wrapper<T> queryWrapper) → 条件查询列表
    // - selectOne(Wrapper<T> queryWrapper)  → 条件查询单个
    // - selectCount(Wrapper<T> queryWrapper) → 计数查询

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




}