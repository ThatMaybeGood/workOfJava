package com.reports.mapper;

import com.reports.dto.response.outpatient.forecast.ForecastDayRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 预测门诊量 Mapper(查询时实时计算)
 * 预测值 = 去年同期日门诊量 × 增长率(近30天÷去年同期30天), 唯一取数是门诊量按日聚合
 */
@Mapper
public interface OutpatientForecastMapper {

    /** 门诊量按日聚合(就诊日归期, 含科室过滤): 前年1-1~昨天一次查全, 近30天/去年同期窗口/去年逐日全在Java内取 */
    List<ForecastDayRow> queryDailyVolumeRange(@Param("startDate") java.sql.Date startDate, @Param("endDate") java.sql.Date endDate,
                                               @Param("deptCode") String deptCode, @Param("deptName") String deptName);
}
