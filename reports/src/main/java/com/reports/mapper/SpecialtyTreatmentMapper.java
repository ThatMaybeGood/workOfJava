package com.reports.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.reports.entity.SpecialtyTreatmentOvEntity;
import com.reports.entity.SpecialtyTreatmentDtlEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 专科治疗量统计 Mapper
 */
@Mapper
public interface SpecialtyTreatmentMapper extends BaseMapper<SpecialtyTreatmentOvEntity> {

    /**
     * 查询专科治疗量概览
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 专科治疗量概览数据
     */
    SpecialtyTreatmentOvEntity queryOverview(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 查询专科治疗量科室明细
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param deptName  科室名称（支持模糊查询）
     * @return 科室明细数据列表
     */
    List<SpecialtyTreatmentDtlEntity> queryDeptDetail(@Param("startDate") Date startDate,
                                                       @Param("endDate") Date endDate,
                                                       @Param("deptName") String deptName);
}
