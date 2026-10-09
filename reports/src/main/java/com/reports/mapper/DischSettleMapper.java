package com.reports.mapper;

import com.reports.dto.response.cash.discharge.settlement.ChartItem;
import com.reports.dto.response.cash.discharge.settlement.OverviewData;
import com.reports.dto.response.cash.discharge.settlement.TableItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 出院结算报表 Mapper
 * 数据源: tr_disch_settle_visit(出院记录) / tr_settle_master(结算主表) / tr_settle_payments(支付方式)
 * 本期/同期一次扫描,同期日期 ADD_MONTHS(+12) 平移回本期轴对比
 */
@Mapper
public interface DischSettleMapper {

    /**
     * 概览: 本期/同期出院人次(总/已结算/未结算) + 结算金额, 同比行内重算
     */
    OverviewData queryOverview(@Param("startDate") Date startDate,
                               @Param("endDate") Date endDate,
                               @Param("lastStartDate") Date lastStartDate,
                               @Param("lastEndDate") Date lastEndDate);

    /**
     * 日/月明细: 按日期轴聚合出院人次与结算金额
     *
     * @param month true=按月聚合, false=按天
     */
    List<TableItem> queryDetail(@Param("startDate") Date startDate,
                                @Param("endDate") Date endDate,
                                @Param("lastStartDate") Date lastStartDate,
                                @Param("lastEndDate") Date lastEndDate,
                                @Param("month") boolean month);

    /**
     * 渠道分析: 结算主表按操作员分窗口/自助机, 本期人次+同比
     */
    List<ChartItem> queryChannelChart(@Param("startDate") Date startDate,
                                      @Param("endDate") Date endDate,
                                      @Param("lastStartDate") Date lastStartDate,
                                      @Param("lastEndDate") Date lastEndDate);

    /**
     * 费别人次分析: 结算主表按结算费别, 本期人次+同比
     */
    List<ChartItem> queryFeeTypeChart(@Param("startDate") Date startDate,
                                      @Param("endDate") Date endDate,
                                      @Param("lastStartDate") Date lastStartDate,
                                      @Param("lastEndDate") Date lastEndDate);

    /**
     * 支付方式金额分析: 支付方式表按支付方式, 本期金额(收)+同比
     */
    List<ChartItem> queryPayTypeChart(@Param("startDate") Date startDate,
                                      @Param("endDate") Date endDate,
                                      @Param("lastStartDate") Date lastStartDate,
                                      @Param("lastEndDate") Date lastEndDate);

    /**
     * 渠道钻取: 该渠道下各结算费别的人次
     */
    List<ChartItem> queryChannelDetail(@Param("startDate") Date startDate,
                                       @Param("endDate") Date endDate,
                                       @Param("channel") String channel);

    /**
     * 支付方式钻取: 该方式的收/退金额
     */
    List<ChartItem> queryPayDetail(@Param("startDate") Date startDate,
                                   @Param("endDate") Date endDate,
                                   @Param("moneyType") String moneyType);
}
