package com.reports.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.reports.entity.NoShowOvEntity;
import com.reports.entity.NoShowDtlEntity;
import com.reports.entity.NoShowOrgEntity;
import com.reports.entity.NoShowChnEntity;
import com.reports.entity.NoShowAgeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 爽约退号分析 Mapper
 */
@Mapper
public interface NoShowMapper extends BaseMapper<NoShowOvEntity> {

    /**
     * 查询爽约退号概览
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 概览数据
     */
    NoShowOvEntity queryOverview(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 查询爽约退号科室明细
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param deptName  科室名称
     * @return 科室明细数据列表
     */
    List<NoShowDtlEntity> queryDeptDetail(@Param("startDate") Date startDate,
                                           @Param("endDate") Date endDate,
                                           @Param("deptName") String deptName);

    /**
     * 查询爽约退号来源分析
     *
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @param originType 来源类型
     * @return 来源分析数据列表
     */
    List<NoShowOrgEntity> queryOriginAnalysis(@Param("startDate") Date startDate,
                                               @Param("endDate") Date endDate,
                                               @Param("originType") String originType);

    /**
     * 查询爽约退号渠道分析
     *
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @param channelType 渠道类型
     * @return 渠道分析数据列表
     */
    List<NoShowChnEntity> queryChannelAnalysis(@Param("startDate") Date startDate,
                                                @Param("endDate") Date endDate,
                                                @Param("channelType") String channelType);

    /**
     * 查询爽约退号年龄分析
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 年龄分析数据列表
     */
    List<NoShowAgeEntity> queryAgeAnalysis(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
