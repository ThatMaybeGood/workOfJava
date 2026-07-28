package com.reports.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.reports.entity.InternetHospitalOvEntity;
import com.reports.entity.InternetHospitalOpEntity;
import com.reports.entity.InternetHospitalBizEntity;
import com.reports.entity.InternetHospitalDeptRnkEntity;
import com.reports.entity.InternetHospitalDocRnkEntity;
import com.reports.entity.InternetHospitalGrwEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 互医质控运营月报 Mapper
 */
@Mapper
public interface InternetHospitalMapper extends BaseMapper<InternetHospitalOvEntity> {

    /**
     * 查询互医质控概览
     *
     * @param statMonth 统计月份
     * @return 互医质控概览数据
     */
    InternetHospitalOvEntity queryOverview(@Param("statMonth") String statMonth);

    /**
     * 查询互医质控运行情况
     *
     * @param statMonth 统计月份
     * @return 互医质控运行情况列表
     */
    List<InternetHospitalOpEntity> queryOperationTable(@Param("statMonth") String statMonth);

    /**
     * 查询互医质控业务分析图表
     *
     * @param statMonth 统计月份
     * @return 互医质控业务分析数据列表
     */
    List<InternetHospitalBizEntity> queryBusinessChart(@Param("statMonth") String statMonth);

    /**
     * 查询互医质控科室排行
     *
     * @param statMonth 统计月份
     * @return 互医质控科室排行列表
     */
    List<InternetHospitalDeptRnkEntity> queryDeptRanking(@Param("statMonth") String statMonth);

    /**
     * 查询互医质控医生排行
     *
     * @param statMonth 统计月份
     * @return 互医质控医生排行列表
     */
    List<InternetHospitalDocRnkEntity> queryDoctorRanking(@Param("statMonth") String statMonth);

    /**
     * 查询互医质控增长趋势
     *
     * @param statMonth 统计月份
     * @return 互医质控增长趋势数据列表
     */
    List<InternetHospitalGrwEntity> queryGrowthChart(@Param("statMonth") String statMonth);
}
