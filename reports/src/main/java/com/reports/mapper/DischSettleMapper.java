package com.reports.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.reports.entity.DischSettleOvEntity;
import com.reports.entity.DischSettleDtlEntity;
import com.reports.entity.DischSettleChtEntity;
import com.reports.dto.response.cash.discharge.settlement.ChartItem;
import com.reports.dto.response.cash.discharge.settlement.OverviewData;
import com.reports.dto.response.cash.discharge.settlement.TableItem;
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

    /**
     * 按月概览：本期与去年同期同范围分别求和后重算同比
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 概览数据
     */
    OverviewData queryOverviewMonth(@Param("startDate") Date startDate,
                                    @Param("endDate") Date endDate);

    /**
     * 按月明细：每月一行，对比按聚合后的本期/上期重算
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 月明细数据
     */
    List<TableItem> queryDetailMonth(@Param("startDate") Date startDate,
                                     @Param("endDate") Date endDate);

    /**
     * 按月图表：构成类按项目汇总，趋势类(AMOUNT_TYPE)按月份汇总
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param chartType 图表类型
     * @return 图表数据
     */
    List<ChartItem> queryChartMonth(@Param("startDate") Date startDate,
                                    @Param("endDate") Date endDate,
                                    @Param("chartType") String chartType);
}
