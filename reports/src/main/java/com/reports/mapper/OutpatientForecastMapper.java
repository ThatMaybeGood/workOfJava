package com.reports.mapper;

import com.reports.entity.OutpatientForecastYearEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 预测门诊量 Mapper(查询时实时计算)
 */
@Mapper
public interface OutpatientForecastMapper {

    /**
     * 近30天挂号量日均
     */
    Double queryAvgReg(@Param("startDate") java.sql.Date startDate, @Param("endDate") java.sql.Date endDate,
                       @Param("deptCode") String deptCode, @Param("deptName") String deptName);

    /**
     * 近30天预约量日均
     */
    Double queryAvgAppoint(@Param("startDate") java.sql.Date startDate, @Param("endDate") java.sql.Date endDate,
                           @Param("deptCode") String deptCode, @Param("deptName") String deptName);

    /**
     * 某日实时预约量(无记录返回空)
     */
    Double queryAppoint(@Param("appointDate") java.sql.Date appointDate,
                        @Param("deptCode") String deptCode, @Param("deptName") String deptName);

    /**
     * 近30天就诊系数 = 1 - 爽约退号率
     */
    Double queryVisitCoef(@Param("startDate") java.sql.Date startDate, @Param("endDate") java.sql.Date endDate,
                          @Param("deptCode") String deptCode, @Param("deptName") String deptName);

    /**
     * 某日天气出勤系数(无数据返回空)
     */
    Double queryWeatherCoef(@Param("weatherDate") java.sql.Date weatherDate);

    /**
     * 某日节假日类型(无记录返回空)
     */
    String queryHolidayType(@Param("holDate") java.sql.Date holDate);

    /**
     * 近一年法定节假日(倒序)
     */
    List<Date> queryRecentHolidays(@Param("startDate") java.sql.Date startDate, @Param("endDate") java.sql.Date endDate);

    /**
     * 某日之前的最近一个正常工作日(排除周末和节假日)
     */
    Date queryPrevWorkday(@Param("beforeDate") java.sql.Date beforeDate);

    /**
     * 某日门诊量
     */
    Integer queryVolume(@Param("statDate") java.sql.Date statDate,
                        @Param("deptCode") String deptCode, @Param("deptName") String deptName);

    /**
     * 某年各月门诊量
     */
    List<OutpatientForecastYearEntity> queryMonthlyVolume(@Param("year") String year,
                                                          @Param("deptCode") String deptCode, @Param("deptName") String deptName);

    /**
     * 时间段内门诊量合计
     */
    Integer queryYtdVolume(@Param("startDate") java.sql.Date startDate, @Param("endDate") java.sql.Date endDate,
                           @Param("deptCode") String deptCode, @Param("deptName") String deptName);
}
