package com.reports.mapper;

import com.reports.entity.SpecialtyTreatmentOvEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.Date;
import java.util.List;

/**
 * 专科治疗量统计 Mapper
 */
@Mapper
public interface SpecialtyTreatmentMapper {

    /**
     * 查询专科治疗量概览
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param deptCode  科室编码（可选）
     * @param deptName  科室名称（支持模糊查询，可选）
     * @return 专科治疗量概览数据
     */
    SpecialtyTreatmentOvEntity queryOverview(@Param("startDate") Date startDate,
                                             @Param("endDate") Date endDate,
                                             @Param("deptCode") String deptCode,
                                             @Param("deptName") String deptName);

    /**
     * 查询专科治疗量科室明细
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param deptName  科室名称（支持模糊查询）
     * @return 科室明细数据列表
     */
    List<SpecialtyTreatmentOvEntity> queryDeptDetail(@Param("startDate") Date startDate,
                                                     @Param("endDate") Date endDate,
                                                     @Param("deptCode") String deptCode,
                                                     @Param("deptName") String deptName);
}
