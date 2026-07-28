package com.reports.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.reports.entity.DischSettleOvEntity;
import com.reports.entity.DischSettleDtlEntity;
import com.reports.entity.DischSettleChtEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 出院结算报表 Mapper
 */
@Mapper
public interface DischSettleMapper extends BaseMapper<DischSettleOvEntity> {

    /**
     * 查询出院结算概览
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 概览数据
     */
    DischSettleOvEntity queryOverview(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 查询出院结算日明细
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 日明细数据
     */
    List<DischSettleDtlEntity> queryDetail(@Param("startDate") Date startDate,
                                            @Param("endDate") Date endDate);

    /**
     * 查询出院结算图表数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param chartType 图表类型
     * @return 图表数据
     */
    List<DischSettleChtEntity> queryChart(@Param("startDate") Date startDate,
                                           @Param("endDate") Date endDate,
                                           @Param("chartType") String chartType);
}
