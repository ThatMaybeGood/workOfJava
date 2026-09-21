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
     * 查询互医质控概览（门诊量直查源表计数，比率取 tr_inet_hosp_ov 分子分母合计后在 impl 相除）
     *
     * @param startDate 起始日期（含），格式 yyyy-MM-dd
     * @param endDate   结束日期（含），格式 yyyy-MM-dd
     * @return 互医质控概览数据
     */
    InternetHospitalOvEntity queryOverview(@Param("startDate") String startDate, @Param("endDate") String endDate);

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
     * 查询互医质控科室接诊量排行（源表直查：已接诊按科室分组，当月+上月一次扫描分别累加，按当月倒序）
     *
     * @param startDate     当月起始日期（含），格式 yyyy-MM-dd
     * @param endDate       当月结束日期（含），格式 yyyy-MM-dd
     * @param lastStartDate 上月起始日期（含），格式 yyyy-MM-dd
     * @param lastEndDate   上月结束日期（含），格式 yyyy-MM-dd
     * @return 科室接诊量排行列表
     */
    List<InternetHospitalDeptRnkEntity> queryDeptRanking(@Param("startDate") String startDate,
                                                         @Param("endDate") String endDate,
                                                         @Param("lastStartDate") String lastStartDate,
                                                         @Param("lastEndDate") String lastEndDate);

    /**
     * 查询互医质控医生接诊量排行（源表直查：已接诊按医生分组计数，倒序）
     *
     * @param startDate 起始日期（含），格式 yyyy-MM-dd
     * @param endDate   结束日期（含），格式 yyyy-MM-dd
     * @return 医生接诊量排行列表
     */
    List<InternetHospitalDocRnkEntity> queryDoctorRanking(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 查询互医质控平均候诊时长科室TOP20（源表直查：reception_time-create_time 平均，分钟）
     *
     * @param startDate 起始日期（含），格式 yyyy-MM-dd
     * @param endDate   结束日期（含），格式 yyyy-MM-dd
     * @return 候诊时长科室排行列表
     */
    List<InternetHospitalGrwEntity> queryGrowthChart(@Param("startDate") String startDate, @Param("endDate") String endDate);
}
