package com.mergedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mergedata.model.entity.YQHolidayEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.cache.annotation.Cacheable;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface HolidayMapper extends BaseMapper<YQHolidayEntity> {

    /*
     * 查询所有节假日
     */
    List<YQHolidayEntity> selectAll();

    /*
     * 根据年份查询节假日信息
     * @param year 节假日年份
     * @return 符合条件的节假日列表
     * 缓存策略：根据年份查询节假日信息，缓存到 "holidays" 缓存中，缓存键为 year 参数值
     */
    @Cacheable(value = "holidays", key = "#year")
    List<YQHolidayEntity> selectByYear(@Param("holidayYear") Integer year);


    /*
     * 根据日期查询节假日信息
     * @Param 指定名称，与 XML 中的 #{holiday_date} 匹配
     */
    List<YQHolidayEntity> selectByDate(@Param("holidayDate") LocalDate date);

    /*
     * 插入单条节假日信息
     * @return 影响的行数
     */
    int insert(YQHolidayEntity holiday); // 修正：改为 insert，返回 int

    /*
     * 批量插入节假日列表
     * @return 影响的行数
     */
    int batchInsertList(List<YQHolidayEntity> holidays); // 修正：返回 int

    /*
     * 更新节假日信息
     * @return 影响的行数
     */
    int delete(String  serialNo); // 修正：返回 int

    int update(@Param("holidayDate") LocalDate date); // 修正：返回 int

}