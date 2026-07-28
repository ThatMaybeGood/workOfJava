package com.reports.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.reports.entity.RoomUsageOvEntity;
import com.reports.entity.RoomUsageDtlEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 诊室使用率分析 Mapper
 */
@Mapper
public interface RoomUsageMapper extends BaseMapper<RoomUsageOvEntity> {

    /**
     * 查询诊室使用率概览
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 诊室使用率概览数据
     */
    RoomUsageOvEntity queryOverview(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 查询诊室使用率科室明细
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param deptName  科室名称（支持模糊查询）
     * @return 科室明细数据列表
     */
    List<RoomUsageDtlEntity> queryDeptDetail(@Param("startDate") Date startDate,
                                              @Param("endDate") Date endDate,
                                              @Param("deptName") String deptName);
}
