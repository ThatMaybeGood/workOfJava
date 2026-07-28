package com.reports.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.reports.entity.MedTechOvEntity;
import com.reports.entity.MedTechDtlEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 医技统计 Mapper
 */
@Mapper
public interface MedTechMapper extends BaseMapper<MedTechOvEntity> {

    /**
     * 查询医技统计概览
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 概览数据
     */
    MedTechOvEntity queryOverview(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 查询医技科室明细
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param deptName  科室名称
     * @return 科室明细数据列表
     */
    List<MedTechDtlEntity> queryDeptDetail(@Param("startDate") Date startDate,
                                            @Param("endDate") Date endDate,
                                            @Param("deptName") String deptName);
}
