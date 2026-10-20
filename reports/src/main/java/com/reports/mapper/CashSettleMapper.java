package com.reports.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.reports.entity.CashSettleOvEntity;
import com.reports.entity.CashSettleDtlEntity;
import com.reports.entity.CashSettleChtEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.Date;
import java.util.List;
import java.util.Map;

/**
 * 收费员结账统计 Mapper
 */
@Mapper
public interface CashSettleMapper extends BaseMapper<CashSettleOvEntity> {

    /**
     * 查询收费员结账概览
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 概览数据
     */
    CashSettleOvEntity queryOverview(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 概览挂号类4指标：源表TR_OUTP_FIN_CLINIC_MASTER本期/同期一次扫描
     *
     * @param startDate    本期开始日期
     * @param endDate      本期结束日期
     * @param lastStartDate 同期开始日期
     * @param lastEndDate   同期结束日期
     * @return 挂号类指标本期/同期计数
     */
    com.reports.dto.response.cash.cashier.settlement.RegisterOverviewData queryRegisterOverview(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("lastStartDate") Date lastStartDate,
            @Param("lastEndDate") Date lastEndDate);

    /**
     * 图表按收费员维度：挂号类4指标按工号过滤只出本期(图表无同比展示)
     *
     * @param startDate 本期开始日期
     * @param endDate   本期结束日期
     * @param cashier   收费员工号(OPERATOR_NO)
     * @return 该收费员挂号类指标本期计数
     */
    com.reports.dto.response.cash.cashier.settlement.RegisterOverviewData queryCashierWorkload(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("cashier") String cashier);

    /**
     * 概览收费类2指标：源表TR_OUTP_FIN_RCPT_ACCT本期/同期一次扫描（口径同门诊财务进项/退项）
     *
     * @param startDate    本期开始日期
     * @param endDate      本期结束日期
     * @param lastStartDate 同期开始日期
     * @param lastEndDate   同期结束日期
     * @return 收费类指标本期/同期计数
     */
    com.reports.dto.response.cash.cashier.settlement.RcptOverviewData queryRcptOverview(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("lastStartDate") Date lastStartDate,
            @Param("lastEndDate") Date lastEndDate);

    /**
     * 图表按收费员维度-收费类2指标：只出本期(图表无同比展示)
     *
     * @param startDate 本期开始日期
     * @param endDate   本期结束日期
     * @param cashier   收费员工号(OPERATOR_NO)
     * @return 该收费员收费类指标本期计数
     */
    com.reports.dto.response.cash.cashier.settlement.RcptOverviewData queryCashierCharge(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("cashier") String cashier);

    /**
     * 概览预交金类2指标：源表tr_inpat_prepay_rcpt本期/同期一次扫描（口径同住院预交金统计）
     *
     * @param startDate    本期开始日期
     * @param endDate      本期结束日期
     * @param lastStartDate 同期开始日期
     * @param lastEndDate   同期结束日期
     * @return 预交金类指标本期/同期计数
     */
    com.reports.dto.response.cash.cashier.settlement.PrepayOverviewData queryPrepayOverview(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("lastStartDate") Date lastStartDate,
            @Param("lastEndDate") Date lastEndDate);

    /**
     * 图表按收费员维度-预交金类2指标：只出本期(图表无同比展示)
     *
     * @param startDate 本期开始日期
     * @param endDate   本期结束日期
     * @param cashier   收费员工号(OPERATOR_NO)
     * @return 该收费员预交金类指标本期计数
     */
    com.reports.dto.response.cash.cashier.settlement.PrepayOverviewData queryCashierPrepay(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("cashier") String cashier);

    /**
     * 概览出院结算量：源表tr_disch_settle_visit本期/同期一次扫描（settle_status=1，口径同出院结算报表已结算人次）
     *
     * @param startDate    本期开始日期
     * @param endDate      本期结束日期
     * @param lastStartDate 同期开始日期
     * @param lastEndDate   同期结束日期
     * @return 出院结算量本期/同期计数
     */
    com.reports.dto.response.cash.cashier.settlement.DischargeOverviewData queryDischargeOverview(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("lastStartDate") Date lastStartDate,
            @Param("lastEndDate") Date lastEndDate);

    /**
     * 图表按收费员维度-出院结算量：只出本期(图表无同比展示)
     *
     * @param startDate 本期开始日期
     * @param endDate   本期结束日期
     * @param cashier   收费员工号(OPERATOR_NO)
     * @return 该收费员出院结算量本期计数
     */
    com.reports.dto.response.cash.cashier.settlement.DischargeOverviewData queryCashierDischarge(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("cashier") String cashier);





    /**
     * source tab 图表：选定日/月+选定指标按工号拆
     *
     * @param chartStartDate 选定日/月起始(含)
     * @param chartEndDate   选定日/月结束(不含)
     * @param metric         指标名(9个中文标签之一)
     * @return (cashierName=工号, itemValue) 行
     */
    List<CashSettleDtlEntity> querySourceChart(@Param("chartStartDate") Date chartStartDate,
                                               @Param("chartEndDate") Date chartEndDate,
                                               @Param("metric") String metric);

    /**
     * 查询收费员结账日明细
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param itemType  项目类型
     * @param month     true按月聚合(每月每收费员每类型一行),false按天
     * @return 日明细数据
     */
    List<CashSettleDtlEntity> queryDetail(@Param("startDate") Date startDate,
                                           @Param("endDate") Date endDate,
                                           @Param("itemType") String itemType,
                                           @Param("month") boolean month);



    /**
     * 工作量5指标·按时间+操作员出7个原始分量（挂号表STAT_DATE归期:当日/取号/预约成功/取消预约;
     * 挂号表RETURNED_DATE归期:退号; 收据表:缴费/退费张数），dimValue 为空不过滤
     */
    List<Map<String, Object>> queryWorkloadPivot(@Param("startDate") Date startDate,
                                                 @Param("endDate") Date endDate,
                                                 @Param("month") boolean month,
                                                 @Param("dimType") String dimType,
                                                 @Param("dimValue") String dimValue);

    /** 工号批量取姓名(staff_code不唯一取MIN): 维度下拉从透视结果派生后回关联, 查不到由调用方回退工号 */
    List<Map<String, Object>> queryStaffNames(@Param("codes") List<String> codes);

    /**
     * 9业务量指标·按时间+操作员出每指标笔数（按收费员统计/按来源方式统计表格共用;
     * 出院结算量用 tr_settle_master 按结算日+工号, 口径同来源图表默认指标）
     */
    List<Map<String, Object>> queryMetricPivot(@Param("startDate") Date startDate,
                                               @Param("endDate") Date endDate,
                                               @Param("month") boolean month,
                                               @Param("dimType") String dimType,
                                               @Param("dimValue") String dimValue);
}
