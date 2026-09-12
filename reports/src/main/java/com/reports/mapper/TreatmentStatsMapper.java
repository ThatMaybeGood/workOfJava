package com.reports.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.reports.entity.TreatmentStatsOvEntity;
import com.reports.entity.TreatmentStatsDtlEntity;
import com.reports.entity.TreatmentStatsTrendEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 治疗统计 Mapper
 */
@Mapper
public interface TreatmentStatsMapper extends BaseMapper<TreatmentStatsOvEntity> {

    /**
     * 查询治疗统计概览
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 概览数据
     */
    TreatmentStatsOvEntity queryOverview(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 查询治疗统计科室明细
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 科室明细数据
     */
    List<TreatmentStatsDtlEntity> queryDeptDetail(@Param("startDate") Date startDate,
                                                    @Param("endDate") Date endDate,
                                                    @Param("deptCode") String deptCode,
                                                    @Param("deptName") String deptName);

    /**
     * 查询治疗统计每日趋势
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 趋势数据
     */
    List<TreatmentStatsTrendEntity> queryTrend(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 查询 TOP 治疗项目（按治疗人次降序，页面取前 10 个）
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 每项 {name, value}
     */
    List<Map<String, Object>> queryTopItems(@Param("startDate") Date startDate,
                                             @Param("endDate") Date endDate);
}
