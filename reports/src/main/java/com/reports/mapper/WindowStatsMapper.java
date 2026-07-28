package com.reports.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.reports.entity.WindowStatsOvEntity;
import com.reports.entity.WindowStatsAgeEntity;
import com.reports.entity.WindowStatsTmEntity;
import com.reports.entity.WindowStatsSrcEntity;
import com.reports.entity.WindowStatsLoadEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 人工窗口统计 Mapper
 */
@Mapper
public interface WindowStatsMapper extends BaseMapper<WindowStatsOvEntity> {

    /**
     * 查询人工窗口统计概览
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 窗口统计概览数据
     */
    WindowStatsOvEntity queryOverview(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 查询人工窗口年龄分析
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 年龄分析数据列表
     */
    List<WindowStatsAgeEntity> queryAgeAnalysis(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 查询人工窗口时段分析
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 时段分析数据列表
     */
    List<WindowStatsTmEntity> queryTimeAnalysis(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 查询人工窗口来源分析
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 来源分析数据列表
     */
    List<WindowStatsSrcEntity> querySourceAnalysis(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 查询人工窗口工作量
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 工作量数据列表
     */
    List<WindowStatsLoadEntity> queryWorkload(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
