package com.reports.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.reports.entity.LabStatsOvEntity;
import com.reports.entity.LabStatsRnkEntity;
import com.reports.entity.LabStatsTmEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 检验统计 Mapper
 */
@Mapper
public interface LabStatsMapper extends BaseMapper<LabStatsOvEntity> {

    /**
     * 查询检验统计概览
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 概览数据
     */
    LabStatsOvEntity queryOverview(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 查询检验统计排行
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param rankType  排行类型
     * @return 排行数据列表
     */
    List<LabStatsRnkEntity> queryRanking(@Param("startDate") Date startDate,
                                          @Param("endDate") Date endDate,
                                          @Param("rankType") String rankType);

    /**
     * 查询检验时段分析
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 时段分析数据列表
     */
    List<LabStatsTmEntity> queryTimeAnalysis(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
