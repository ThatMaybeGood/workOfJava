package com.reports.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.reports.entity.OutpatientOperationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import com.reports.entity.OutpatientOpDtlEntity;
import java.util.List;

/**
 * 门诊运行数据 Mapper
 */
@Mapper
public interface OutpatientOperationMapper extends BaseMapper<OutpatientOperationEntity> {

    /**
     * 查询指定时间范围和科室的统计数据汇总
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param deptCode  科室编码（可为空，为空则查所有科室）
     * @return 汇总数据
     */
    OutpatientOperationEntity querySummaryByDateAndDept(@Param("startDate") Date startDate,
                                                        @Param("endDate") Date endDate,
                                                        @Param("deptCode") String deptCode);

    /**
     * 根据科室分组统计指定时间范围的数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param deptCode  科室编码（可为空，为空则查所有科室）
     * @return 按科室分组统计结果
     */
    List<OutpatientOperationEntity> queryGroupByDept(@Param("startDate") Date startDate,
                                                      @Param("endDate") Date endDate,
                                                      @Param("deptCode") String deptCode);

    /**
     * 查询科室明细数据
     */
    List<OutpatientOpDtlEntity> queryDeptDetail(@Param("startDate") Date startDate,
                                                 @Param("endDate") Date endDate,
                                                 @Param("deptName") String deptName);

}
