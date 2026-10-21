package com.reports.mapper;

import com.reports.dto.response.outpatient.forecast.ForecastDayRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 预测门诊量 Mapper(查询时实时计算)
 * 取数原则: 同表不同日期范围的聚合合并为一次按日聚合查询, 区间指标在Java内计算
 */
@Mapper
public interface OutpatientForecastMapper {

    /** 门诊量按日聚合(就诊日归期, 含科室过滤): 总挂号行数+预约行数(indicator=1 或 indicator=2且挂号日!=就诊日), 日/年预测及节假日系数全部基础量一次查全 */
    List<ForecastDayRow> queryDailyVolumeRange(@Param("startDate") java.sql.Date startDate, @Param("endDate") java.sql.Date endDate,
                                               @Param("deptCode") String deptCode, @Param("deptName") String deptName);

    /** 近30天就诊系数 = 1 - 爽约退号率(退号按退号日归期, 与按日聚合口径不同, 独立查询) */
    Double queryVisitCoef(@Param("startDate") java.sql.Date startDate, @Param("endDate") java.sql.Date endDate,
                          @Param("deptCode") String deptCode, @Param("deptName") String deptName);

    /** 日期区间内每日天气出勤系数 */
    List<ForecastDayRow> queryWeatherRange(@Param("startDate") java.sql.Date startDate, @Param("endDate") java.sql.Date endDate);

    /** 日期区间内法定节假日(yyyy-MM-dd, 近一年样本+未来30天判断一次查全) */
    List<String> queryLegalHolidayRange(@Param("startDate") java.sql.Date startDate, @Param("endDate") java.sql.Date endDate);
}
