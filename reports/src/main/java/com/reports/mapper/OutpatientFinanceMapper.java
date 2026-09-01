package com.reports.mapper;

import com.reports.entity.cash.OutpFinanceClinicMaster;
import com.reports.entity.cash.OutpFinanceRcptAcct;
import com.reports.entity.cash.OutpFinancePaymentsMoney;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 门诊财务报表 Mapper
 * <p>读取 ETL 抽取的 5 张明细存储，指标计算与人次去重均在 Service 完成。</p>
 */
@Mapper
public interface OutpatientFinanceMapper {

    /**
     * 门诊量：按周期净量分组（未退号 − 退号），T2 未退号 / T3 退号
     */
    List<Map<String, Object>> queryClinicCounts(@Param("statisticType") Integer statisticType,
                                                @Param("startDate") String startDate,
                                                @Param("endDate") String endDate,
                                                @Param("timeType") Integer timeType);

    /**
     * 收据张数/金额：按周期分组聚合（TR_OUTP_FIN_ACCT_MASTER）
     */
    List<Map<String, Object>> queryAcctAmounts(@Param("statisticType") Integer statisticType,
                                               @Param("startDate") String startDate,
                                               @Param("endDate") String endDate,
                                               @Param("timeType") Integer timeType);

    /**
     * 门诊收据原始行（人次去重在 Service，只负责按 statisticType 过滤+排序）
     */
    List<OutpFinanceRcptAcct> queryRcptRows(@Param("statisticType") Integer statisticType,
                                            @Param("startDate") String startDate,
                                            @Param("endDate") String endDate,
                                            @Param("timeType") Integer timeType);

    /**
     * 门诊挂号原始行（bt8 分界前挂号费计算）
     */
    List<OutpFinanceClinicMaster> queryClinicRows(@Param("statisticType") Integer statisticType,
                                                  @Param("startDate") String startDate,
                                                  @Param("endDate") String endDate,
                                                  @Param("timeType") Integer timeType);

    /**
     * bt1/bt3/bt4 订单来源/订单渠道/人次渠道：按操作员净计数
     */
    List<Map<String, Object>> queryRcptCountByOperator(@Param("statisticType") Integer statisticType,
                                                       @Param("startDate") String startDate,
                                                       @Param("endDate") String endDate,
                                                       @Param("timeType") Integer timeType);

    /**
     * bt6 渠道金额：按操作员求和（进项正+退项负）
     */
    List<Map<String, Object>> queryRcptSumByOperator(@Param("statisticType") Integer statisticType,
                                                     @Param("startDate") String startDate,
                                                     @Param("endDate") String endDate,
                                                     @Param("timeType") Integer timeType);

    /**
     * bt2 取号渠道：MOP_QUEUE 直接查（reserve+used + 退号者计入净量）
     */
    List<Map<String, Object>> queryQueueCountByOperator(@Param("statisticType") Integer statisticType,
                                                        @Param("startDate") String startDate,
                                                        @Param("endDate") String endDate,
                                                        @Param("timeType") Integer timeType);

    /**
     * bt5 人次支付：按支付方式统计人次（PAYMENTS_MONEY ⋈ RCPT_ACCT，同一患者同一收据去重）
     */
    List<Map<String, Object>> queryPaymentCountByMoneyType(@Param("statisticType") Integer statisticType,
                                                           @Param("startDate") String startDate,
                                                           @Param("endDate") String endDate,
                                                           @Param("timeType") Integer timeType);

    /**
     * bt7 支付方式金额：按支付方式求和（PAYMENTS_MONEY ⋈ RCPT_ACCT）
     */
    List<Map<String, Object>> queryPaymentSumByMoneyType(@Param("statisticType") Integer statisticType,
                                                         @Param("startDate") String startDate,
                                                         @Param("endDate") String endDate,
                                                         @Param("timeType") Integer timeType);

    /**
     * bt9 应收/实收：按支付方式归类求和（PAYMENTS_MONEY ⋈ RCPT_ACCT）
     */
    List<Map<String, Object>> queryPaymentSumByCategory(@Param("statisticType") Integer statisticType,
                                                        @Param("startDate") String startDate,
                                                        @Param("endDate") String endDate,
                                                        @Param("timeType") Integer timeType);

}
