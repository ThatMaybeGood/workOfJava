package com.reports.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.reports.entity.OutpatientAlertOvEntity;
import com.reports.entity.OutpatientAlertDeptEntity;
import com.reports.entity.OutpatientAlertDocEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 门诊预警统计 Mapper
 */
@Mapper
public interface OutpatientAlertMapper extends BaseMapper<OutpatientAlertOvEntity> {

    /**
     * 查询门诊预警概览数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 门诊预警概览数据
     */
    OutpatientAlertOvEntity queryOverview(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 查询门诊预警科室明细
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param deptName  科室名称
     * @return 门诊预警科室明细列表
     */
    List<OutpatientAlertDeptEntity> queryDeptDetail(@Param("startDate") Date startDate,
                                                     @Param("endDate") Date endDate,
                                                     @Param("deptName") String deptName);

    /**
     * 查询门诊预警医生明细
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param deptName  科室名称
     * @return 门诊预警医生明细列表
     */
    List<OutpatientAlertDocEntity> queryDoctorDetail(@Param("startDate") Date startDate,
                                                      @Param("endDate") Date endDate,
                                                      @Param("deptName") String deptName);
}
