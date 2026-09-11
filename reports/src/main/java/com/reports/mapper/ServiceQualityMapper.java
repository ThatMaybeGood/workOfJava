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
     * 查询门诊服务质量概览（按投诉/表扬明细统计，支持科室过滤）
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param deptCode  科室编码（可选）
     * @param deptName  科室名称（可选）
     * @return 服务质量概览数据
     */
    ServiceQualityOvEntity queryOverview(@Param("startDate") Date startDate,
                                         @Param("endDate") Date endDate,
                                         @Param("deptCode") String deptCode,
                                         @Param("deptName") String deptName);

    /**
     * 查询门诊服务质量投诉列表
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param deptCode  科室编码（可选）
     * @param deptName  科室名称（支持模糊查询）
     * @return 投诉列表数据
     */
    List<ServiceQualityCmplEntity> queryComplaintList(@Param("startDate") Date startDate,
                                                       @Param("endDate") Date endDate,
                                                       @Param("deptCode") String deptCode,
                                                       @Param("deptName") String deptName);

    /**
     * 查询门诊服务质量表扬列表
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param deptCode  科室编码（可选）
     * @param deptName  科室名称（支持模糊查询）
     * @return 表扬列表数据
     */
    List<ServiceQualityPrzEntity> queryPraiseList(@Param("startDate") Date startDate,
                                                   @Param("endDate") Date endDate,
                                                   @Param("deptCode") String deptCode,
                                                   @Param("deptName") String deptName);

    /**
     * 按日期范围查询投诉明细（数据维护用）
     */
    List<ServiceQualityCmplEntity> queryComplaintByDate(@Param("startDate") Date startDate,
                                                        @Param("endDate") Date endDate);

    /**
     * 按日期范围查询表扬明细（数据维护用）
     */
    List<ServiceQualityPrzEntity> queryPraiseByDate(@Param("startDate") Date startDate,
                                                    @Param("endDate") Date endDate);

    /**
     * 新增投诉明细
     */
    int insertComplaint(ServiceQualityCmplEntity entity);

    /**
     * 更新投诉明细
     */
    int updateComplaint(ServiceQualityCmplEntity entity);

    /**
     * 删除投诉明细
     */
    int deleteComplaintById(@Param("id") Long id);

    /**
     * 新增表扬明细
     */
    int insertPraise(ServiceQualityPrzEntity entity);

    /**
     * 更新表扬明细
     */
    int updatePraise(ServiceQualityPrzEntity entity);

    /**
     * 删除表扬明细
     */
    int deletePraiseById(@Param("id") Long id);
}
