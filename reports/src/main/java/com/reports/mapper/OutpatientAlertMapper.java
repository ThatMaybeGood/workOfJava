package com.reports.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.reports.entity.OutpatientAlertOvEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.Date;
import java.util.List;

/**
 * 门诊预警统计 Mapper（单表 TR_OUTP_ALT_OV）
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
     * 查询门诊预警科室汇总
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param deptCode  科室编码（可选）
     * @param deptName  科室名称（可选，模糊）
     * @return 科室汇总列表
     */
    List<OutpatientAlertOvEntity> queryDeptDetail(@Param("startDate") Date startDate,
                                                  @Param("endDate") Date endDate,
                                                  @Param("deptCode") String deptCode,
                                                  @Param("deptName") String deptName);

    /**
     * 查询门诊预警医生汇总
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param deptCode  科室编码（可选）
     * @param deptName  科室名称（可选，模糊）
     * @return 医生汇总列表
     */
    List<OutpatientAlertOvEntity> queryDoctorDetail(@Param("startDate") Date startDate,
                                                    @Param("endDate") Date endDate,
                                                    @Param("deptCode") String deptCode,
                                                    @Param("deptName") String deptName);

    /**
     * 查询早退明细（不分页，弹窗用）
     *
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @param deptCode   科室编码（可选）
     * @param deptName   科室名称（可选，模糊）
     * @param doctorName 医生姓名（可选，模糊）
     * @param clinicPeriod 出诊时间段（可选，模糊）
     * @return 早退明细列表
     */
    List<OutpatientAlertOvEntity> queryEarlyLeaveDetail(@Param("startDate") Date startDate,
                                                        @Param("endDate") Date endDate,
                                                        @Param("deptCode") String deptCode,
                                                        @Param("deptName") String deptName,
                                                        @Param("doctorName") String doctorName,
                                                        @Param("clinicPeriod") String clinicPeriod);
}
