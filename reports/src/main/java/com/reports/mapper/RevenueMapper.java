package com.reports.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.reports.entity.RevenueOvEntity;
import com.reports.entity.RevenueDeptEntity;
import com.reports.entity.RevenueDocEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 门诊收入分析 Mapper
 */
@Mapper
public interface RevenueMapper extends BaseMapper<RevenueOvEntity> {

    /**
     * 查询门诊收入概览
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param deptCode  科室编码（可选）
     * @param deptName  科室名称（可选，支持模糊查询）
     * @return 收入概览数据
     */
    RevenueOvEntity queryOverview(@Param("startDate") Date startDate,
                                   @Param("endDate") Date endDate,
                                   @Param("deptCode") String deptCode,
                                   @Param("deptName") String deptName);

    /**
     * 查询门诊收入科室明细
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param deptCode  科室编码（可选）
     * @param deptName  科室名称（可选，支持模糊查询）
     * @return 科室收入明细数据列表
     */
    List<RevenueDeptEntity> queryDeptDetail(@Param("startDate") Date startDate,
                                             @Param("endDate") Date endDate,
                                             @Param("deptCode") String deptCode,
                                             @Param("deptName") String deptName);

    /**
     * 查询门诊收入医生明细
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param deptCode  科室编码（可选）
     * @param deptName  科室名称（可选，支持模糊查询）
     * @return 医生收入明细数据列表
     */
    List<RevenueDocEntity> queryDoctorDetail(@Param("startDate") Date startDate,
                                              @Param("endDate") Date endDate,
                                              @Param("deptCode") String deptCode,
                                              @Param("deptName") String deptName);
}
