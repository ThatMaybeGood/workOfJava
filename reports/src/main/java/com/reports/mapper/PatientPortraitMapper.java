package com.reports.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.reports.entity.OutpatientPatientPortraitAgeEntity;
import com.reports.entity.OutpatientPatientPortraitInsurEntity;
import com.reports.entity.OutpatientPatientPortraitIdtyEntity;
import com.reports.entity.OutpatientPatientPortraitRegEntity;
import com.reports.entity.OutpatientPatientPortraitArcEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 患者画像 Mapper
 */
@Mapper
public interface PatientPortraitMapper extends BaseMapper<OutpatientPatientPortraitAgeEntity> {

    /**
     * 查询患者画像年龄分析
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 年龄分析数据
     */
    List<OutpatientPatientPortraitAgeEntity> queryAgeAnalysis(@Param("startDate") Date startDate,
                                                               @Param("endDate") Date endDate);

    /**
     * 查询患者画像医保分析
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 医保分析数据
     */
    List<OutpatientPatientPortraitInsurEntity> queryInsuranceAnalysis(@Param("startDate") Date startDate,
                                                                       @Param("endDate") Date endDate);

    /**
     * 查询患者画像身份分析
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 身份分析数据
     */
    List<OutpatientPatientPortraitIdtyEntity> queryIdentityAnalysis(@Param("startDate") Date startDate,
                                                                     @Param("endDate") Date endDate);

    /**
     * 查询患者画像挂号来源分析
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 挂号来源分析数据
     */
    List<OutpatientPatientPortraitRegEntity> queryRegOriginAnalysis(@Param("startDate") Date startDate,
                                                                     @Param("endDate") Date endDate);

    /**
     * 查询患者画像建档来源分析
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 建档来源分析数据
     */
    List<OutpatientPatientPortraitArcEntity> queryArcOriginAnalysis(@Param("startDate") Date startDate,
                                                                     @Param("endDate") Date endDate);
}
