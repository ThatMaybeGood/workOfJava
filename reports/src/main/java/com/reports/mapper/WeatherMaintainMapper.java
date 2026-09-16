package com.reports.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.reports.entity.WeatherEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 天气数据维护 Mapper
 */
@Mapper
public interface WeatherMaintainMapper extends BaseMapper<WeatherEntity> {

    /**
     * 按日期范围查询天气列表
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 天气列表
     */
    List<WeatherEntity> queryWeatherByDate(@Param("startDate") Date startDate,
                                           @Param("endDate") Date endDate);

    /**
     * 近30天同天气日期的爽约退号率(爽约+退号)/挂号量
     *
     * @param weatherType 天气类型
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @return 爽约退号率，无数据返回空
     */
    Double queryNoShowRateByWeather(@Param("weatherType") String weatherType,
                                    @Param("startDate") Date startDate,
                                    @Param("endDate") Date endDate);

    /**
     * 覆盖保存天气（按 weather_date 存在即更新、不存在即插入）
     *
     * @param entity 天气记录
     * @return 影响行数
     */
    int mergeWeather(WeatherEntity entity);

    /**
     * 按日期删除天气
     *
     * @param weatherDate 天气日期
     * @return 影响行数
     */
    int deleteWeatherByDate(@Param("weatherDate") Date weatherDate);
}
