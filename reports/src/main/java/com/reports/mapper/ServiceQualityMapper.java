package com.reports.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.reports.entity.ServiceQualityOvEntity;
import com.reports.entity.ServiceQualityCmplEntity;
import com.reports.entity.ServiceQualityPrzEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 门诊服务质量分析 Mapper
 */
@Mapper
public interface ServiceQualityMapper extends BaseMapper<ServiceQualityOvEntity> {

    /**
     * 查询门诊服务质量概览
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 服务质量概览数据
     */
    ServiceQualityOvEntity queryOverview(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 查询门诊服务质量投诉列表
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param deptName  科室名称（支持模糊查询）
     * @return 投诉列表数据
     */
    List<ServiceQualityCmplEntity> queryComplaintList(@Param("startDate") Date startDate,
                                                       @Param("endDate") Date endDate,
                                                       @Param("deptName") String deptName);

    /**
     * 查询门诊服务质量表扬列表
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param deptName  科室名称（支持模糊查询）
     * @return 表扬列表数据
     */
    List<ServiceQualityPrzEntity> queryPraiseList(@Param("startDate") Date startDate,
                                                   @Param("endDate") Date endDate,
                                                   @Param("deptName") String deptName);
}
