package com.reports.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.reports.entity.OutpatientForecastOvEntity;
import com.reports.entity.OutpatientForecastMonthEntity;
import com.reports.entity.OutpatientForecastYearEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 预测门诊量 Mapper
 */
@Mapper
public interface OutpatientForecastMapper extends BaseMapper<OutpatientForecastOvEntity> {

    /**
     * 查询预测门诊量概览
     *
     * @param statDate 统计日期
     * @return 预测门诊量概览数据
     */
    OutpatientForecastOvEntity queryOverview(@Param("statDate") Date statDate);

    /**
     * 查询30天预测门诊量明细
     *
     * @param statDate 统计日期
     * @return 30天预测门诊量明细列表
     */
    List<OutpatientForecastMonthEntity> queryMonthForecast(@Param("statDate") Date statDate);

    /**
     * 查询12个月预测门诊量明细
     *
     * @param statDate 统计日期
     * @return 12个月预测门诊量明细列表
     */
    List<OutpatientForecastYearEntity> queryYearForecast(@Param("statDate") Date statDate);
}
