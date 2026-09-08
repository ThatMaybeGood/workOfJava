package com.reports.mapper;

import com.reports.annotation.EtlTask;
import com.reports.constant.EtlTaskConst;
import com.reports.entity.cash.OutpFinancePaymentsMoney;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 门诊财务报表 Mapper
 * <p>读取 ETL 抽取的 5 张明细存储；人次去重由 queryVisitCounts 在库内完成，其余指标计算在 Service。</p>
 */
@Mapper
public interface OutpatientFinanceMapper {

    /**
     * 门诊量：按周期净量分组（未退号 − 退号），T2 未退号 / T3 退号
     */
    @EtlTask(
            taskToken = EtlTaskConst.OUTP_FINANCE_CLINIC,
            rangeStart = "startDate",
            rangeEnd = "endDate",
            rangeTarget = "statDate",
            rangeParsePattern = "yyyy-MM",   // 入参是月格式时声明
            rangePattern = "yyyy-MM-dd"      // 传给 ETL 的每天格式
    )
    List<Map<String, Object>> queryClinicCounts(@Param("statisticType") Integer statisticType,
                                                @Param("startDate") Date startDate,
                                                @Param("endDate") Date endDate,
                                                @Param("timeType") Integer timeType);

    /**
     * 收据张数/金额：按周期分组聚合（TR_OUTP_FIN_ACCT_MASTER）
     */
    @EtlTask(
            taskToken = EtlTaskConst.OUTP_FINANCE_ACCT,
            rangeStart = "startDate",
            rangeEnd = "endDate",
            rangeTarget = "statDate",
            rangeParsePattern = "yyyy-MM",   // 入参是月格式时声明
            rangePattern = "yyyy-MM-dd"      // 传给 ETL 的每天格式
    )
    List<Map<String, Object>> queryAcctAmounts(@Param("statisticType") Integer statisticType,
                                               @Param("startDate") Date startDate,
                                               @Param("endDate") Date endDate,
                                               @Param("timeType") Integer timeType);

    /**
     * 缴费人次：同患者+同日+同前缀+票号连续合并为一人次（窗口函数在库内完成，只返回周期+人次）
     */
    @EtlTask(
            taskToken = EtlTaskConst.OUTP_FINANCE_RCPT,
            rangeStart = "startDate",
            rangeEnd = "endDate",
            rangeTarget = "statDate",
            rangeParsePattern = "yyyy-MM",   // 入参是月格式时声明
            rangePattern = "yyyy-MM-dd"      // 传给 ETL 的每天格式
    )
    List<Map<String, Object>> queryVisitCounts(@Param("statisticType") Integer statisticType,
                                               @Param("startDate") Date startDate,
                                               @Param("endDate") Date endDate,
                                               @Param("timeType") Integer timeType);

    /**
     * bt8 业务类型金额：挂号费(分界前)+收据 bill_class 分类
     */
    @EtlTask(
            taskToken = EtlTaskConst.OUTP_FINANCE_RCPT,
            rangeStart = "startDate",
            rangeEnd = "endDate",
            rangeTarget = "statDate",
            rangeParsePattern = "yyyy-MM",   // 入参是月格式时声明
            rangePattern = "yyyy-MM-dd"      // 传给 ETL 的每天格式
    )
    List<Map<String, Object>> queryBizTypeAmount(@Param("statisticType") Integer statisticType,
                                                 @Param("startDate") Date startDate,
                                                 @Param("endDate") Date endDate,
                                                 @Param("timeType") Integer timeType);

    /**
     * bt1 订单来源：门诊挂号表按 SOURCE_TYPE 净人次（未退号+1，退号-1）
     */
    @EtlTask(
            taskToken = EtlTaskConst.OUTP_FINANCE_MOP_QUEUE,
            rangeStart = "startDate",
            rangeEnd = "endDate",
            rangeTarget = "statDate",
            rangeParsePattern = "yyyy-MM",   // 入参是月格式时声明
            rangePattern = "yyyy-MM-dd"      // 传给 ETL 的每天格式
    )
    List<Map<String, Object>> queryClinicCountBySource(@Param("statisticType") Integer statisticType,
                                                         @Param("startDate") Date startDate,
                                                         @Param("endDate") Date endDate,
                                                         @Param("timeType") Integer timeType);

    /**
     * bt3 订单渠道 ：按操作员净张数（收据表）
     */
    @EtlTask(
            taskToken = EtlTaskConst.OUTP_FINANCE_CLINIC,
            rangeStart = "startDate",
            rangeEnd = "endDate",
            rangeTarget = "statDate",
            rangeParsePattern = "yyyy-MM",   // 入参是月格式时声明
            rangePattern = "yyyy-MM-dd"      // 传给 ETL 的每天格式
    )
    List<Map<String, Object>> queryClinicCountByOperator(@Param("statisticType") Integer statisticType,
                                                       @Param("startDate") Date startDate,
                                                       @Param("endDate") Date endDate,
                                                       @Param("timeType") Integer timeType);

    /**
     * bt4 缴费人次渠道：按操作员分类（queryVisitCounts 同口径）
     */
    @EtlTask(
            taskToken = EtlTaskConst.OUTP_FINANCE_RCPT,
            rangeStart = "startDate",
            rangeEnd = "endDate",
            rangeTarget = "statDate",
            rangeParsePattern = "yyyy-MM",   // 入参是月格式时声明
            rangePattern = "yyyy-MM-dd"      // 传给 ETL 的每天格式
    )
    List<Map<String, Object>> queryRcptCountByOperator(@Param("statisticType") Integer statisticType,
                                                       @Param("startDate") Date startDate,
                                                       @Param("endDate") Date endDate,
                                                       @Param("timeType") Integer timeType);

    /**
     * bt6 收据张数渠道：结账主表按操作员分类（张数口径）
     */
    @EtlTask(
            taskToken = EtlTaskConst.OUTP_FINANCE_ACCT,
            rangeStart = "startDate",
            rangeEnd = "endDate",
            rangeTarget = "statDate",
            rangeParsePattern = "yyyy-MM",   // 入参是月格式时声明
            rangePattern = "yyyy-MM-dd"      // 传给 ETL 的每天格式
    )
    List<Map<String, Object>> queryRcptSumByOperator(@Param("statisticType") Integer statisticType,
                                                     @Param("startDate") Date startDate,
                                                     @Param("endDate") Date endDate,
                                                     @Param("timeType") Integer timeType);

    /**
     * bt2 取号渠道：挂号表按操作员净人次（IS_RETURN_TYPE 口径，与 queryClinicCounts 一致）
     */
    @EtlTask(
            taskToken = EtlTaskConst.OUTP_FINANCE_CLINIC,
            rangeStart = "startDate",
            rangeEnd = "endDate",
            rangeTarget = "statDate",
            rangeParsePattern = "yyyy-MM",   // 入参是月格式时声明
            rangePattern = "yyyy-MM-dd"      // 传给 ETL 的每天格式
    )
    List<Map<String, Object>> queryQueueCountByOperator(@Param("statisticType") Integer statisticType,
                                                        @Param("startDate") Date startDate,
                                                        @Param("endDate") Date endDate,
                                                        @Param("timeType") Integer timeType);

    /**
     * bt5 缴费人次支付：按支付方式求金额
     */
    @EtlTask(
            taskToken = EtlTaskConst.OUTP_FINANCE_PAYMENTS_MONEY,
            rangeStart = "startDate",
            rangeEnd = "endDate",
            rangeTarget = "statDate",
            rangeParsePattern = "yyyy-MM",   // 入参是月格式时声明
            rangePattern = "yyyy-MM-dd"      // 传给 ETL 的每天格式
    )
    List<Map<String, Object>> queryPaymentCountByMoneyType(@Param("statisticType") Integer statisticType,
                                                           @Param("startDate") Date startDate,
                                                           @Param("endDate") Date endDate,
                                                           @Param("timeType") Integer timeType);

    /**
     * bt7 支付方式金额：结账主表⋈结账支付表
     */
    @EtlTask(
            taskToken = EtlTaskConst.OUTP_FINANCE_ACCT_MONEY,
            rangeStart = "startDate",
            rangeEnd = "endDate",
            rangeTarget = "statDate",
            rangeParsePattern = "yyyy-MM",   // 入参是月格式时声明
            rangePattern = "yyyy-MM-dd"      // 传给 ETL 的每天格式
    )
    List<Map<String, Object>> queryPaymentSumByMoneyType(@Param("statisticType") Integer statisticType,
                                                         @Param("startDate") Date startDate,
                                                         @Param("endDate") Date endDate,
                                                         @Param("timeType") Integer timeType);

    /**
     * bt9 应收/实收：结账支付按类别归类求和
     */
    @EtlTask(
            taskToken = EtlTaskConst.OUTP_FINANCE_ACCT_MONEY,
            rangeStart = "startDate",
            rangeEnd = "endDate",
            rangeTarget = "statDate",
            rangeParsePattern = "yyyy-MM",   // 入参是月格式时声明
            rangePattern = "yyyy-MM-dd"      // 传给 ETL 的每天格式
    )
    List<Map<String, Object>> queryPaymentSumByCategory(@Param("statisticType") Integer statisticType,
                                                        @Param("startDate") Date startDate,
                                                        @Param("endDate") Date endDate,
                                                        @Param("timeType") Integer timeType);

    /**
     * bt10 应收金额：应收账款类（ELSE 桶）按支付方式明细
     */
    @EtlTask(
            taskToken = EtlTaskConst.OUTP_FINANCE_PAYMENTS_MONEY,
            rangeStart = "startDate",
            rangeEnd = "endDate",
            rangeTarget = "statDate",
            rangeParsePattern = "yyyy-MM",   // 入参是月格式时声明
            rangePattern = "yyyy-MM-dd"      // 传给 ETL 的每天格式
    )
    List<Map<String, Object>> queryPaymentSumReceivable(@Param("statisticType") Integer statisticType,
                                                        @Param("startDate") Date startDate,
                                                        @Param("endDate") Date endDate,
                                                        @Param("timeType") Integer timeType);

    /**
     * bt11 收入金额渠道：结账主表按操作员分类（总收入口径）
     */
    @EtlTask(
            taskToken = EtlTaskConst.OUTP_FINANCE_ACCT,
            rangeStart = "startDate",
            rangeEnd = "endDate",
            rangeTarget = "statDate",
            rangeParsePattern = "yyyy-MM",   // 入参是月格式时声明
            rangePattern = "yyyy-MM-dd"      // 传给 ETL 的每天格式
    )
    List<Map<String, Object>> queryIncomeSumByOperator(@Param("statisticType") Integer statisticType,
                                                       @Param("startDate") Date startDate,
                                                       @Param("endDate") Date endDate,
                                                       @Param("timeType") Integer timeType);

    /**
     * bt12 收入金额支付：结账主表⋈结账支付表（总收入口径）
     */
    @EtlTask(
            taskToken = EtlTaskConst.OUTP_FINANCE_ACCT_MONEY,
            rangeStart = "startDate",
            rangeEnd = "endDate",
            rangeTarget = "statDate",
            rangeParsePattern = "yyyy-MM",   // 入参是月格式时声明
            rangePattern = "yyyy-MM-dd"      // 传给 ETL 的每天格式
    )
    List<Map<String, Object>> queryIncomeSumByMoneyType(@Param("statisticType") Integer statisticType,
                                                        @Param("startDate") Date startDate,
                                                        @Param("endDate") Date endDate,
                                                        @Param("timeType") Integer timeType);

}
