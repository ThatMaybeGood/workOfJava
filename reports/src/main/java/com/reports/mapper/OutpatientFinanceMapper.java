package com.reports.mapper;

import com.reports.entity.EtlClinicEntity;
import com.reports.entity.EtlOutpRcptEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 门诊财务报表 Mapper
 * <p>读取 ETL 抽取的明细存储（ETL_CLINIC / ETL_OUTP_ACCT / ETL_OUTP_RCPT /
 * ETL_OUTP_PAYMENT / ETL_TIMESCHEDULE_QUEUE），指标计算与人次去重均在 Service 完成。</p>
 */
@Mapper
public interface OutpatientFinanceMapper {

    /**
     * 门诊量：按周期分组计数（T2 未退号 / T3 退号，取自 ETL_CLINIC）
     *
     * @return [{period, cnt}]
     */
    List<Map<String, Object>> queryClinicCounts(@Param("statisticType") Integer statisticType,
                                                @Param("startDate") String startDate,
                                                @Param("endDate") String endDate,
                                                @Param("timeType") Integer timeType);

    /**
     * 收据张数/金额：按周期分组聚合（口径 outp_acct_master）
     *
     * @return [{period, amount, receipt}]
     */
    List<Map<String, Object>> queryAcctAmounts(@Param("statisticType") Integer statisticType,
                                               @Param("startDate") String startDate,
                                               @Param("endDate") String endDate,
                                               @Param("timeType") Integer timeType);

    /**
     * 门诊收据原始行（人次去重在 Service，需 patient_id / rcpt_no / visit_date 连续收据规则）
     */
    List<EtlOutpRcptEntity> queryRcptRows(@Param("statisticType") Integer statisticType,
                                          @Param("startDate") String startDate,
                                          @Param("endDate") String endDate,
                                          @Param("timeType") Integer timeType);

    /**
     * 门诊挂号原始行（bt8 分界前挂号费计算）
     */
    List<EtlClinicEntity> queryClinicRows(@Param("statisticType") Integer statisticType,
                                          @Param("startDate") String startDate,
                                          @Param("endDate") String endDate,
                                          @Param("timeType") Integer timeType);

    /**
     * bt1/bt3/bt4 订单来源/订单渠道/人次渠道：按操作员计数（取自 ETL_OUTP_RCPT）
     *
     * @return [{name, cnt}]
     */
    List<Map<String, Object>> queryRcptCountByOperator(@Param("statisticType") Integer statisticType,
                                                       @Param("startDate") String startDate,
                                                       @Param("endDate") String endDate,
                                                       @Param("timeType") Integer timeType);

    /**
     * bt6 渠道金额：按操作员求和（取自 ETL_OUTP_RCPT）
     *
     * @return [{name, amount}]
     */
    List<Map<String, Object>> queryRcptSumByOperator(@Param("statisticType") Integer statisticType,
                                                     @Param("startDate") String startDate,
                                                     @Param("endDate") String endDate,
                                                     @Param("timeType") Integer timeType);

    /**
     * bt2 取号渠道：clinic ⋈ timeschedule_queue 按操作员计数（queue_type='reserve' is_used='used'）
     *
     * @return [{name, cnt}]
     */
    List<Map<String, Object>> queryQueueCountByOperator(@Param("statisticType") Integer statisticType,
                                                        @Param("startDate") String startDate,
                                                        @Param("endDate") String endDate,
                                                        @Param("timeType") Integer timeType);

    /**
     * bt5 人次支付：按支付方式统计人次（同一患者同一收据去重）
     *
     * @return [{name, cnt}]
     */
    List<Map<String, Object>> queryPaymentCountByMoneyType(@Param("statisticType") Integer statisticType,
                                                           @Param("startDate") String startDate,
                                                           @Param("endDate") String endDate,
                                                           @Param("timeType") Integer timeType);

    /**
     * bt7 支付方式金额：按支付方式求和（取自 ETL_OUTP_PAYMENT）
     *
     * @return [{name, amount}]
     */
    List<Map<String, Object>> queryPaymentSumByMoneyType(@Param("statisticType") Integer statisticType,
                                                         @Param("startDate") String startDate,
                                                         @Param("endDate") String endDate,
                                                         @Param("timeType") Integer timeType);

    /**
     * bt9 应收/实收：按支付方式归类求和（取自 ETL_OUTP_PAYMENT）
     *
     * @return [{name, amount}]
     */
    List<Map<String, Object>> queryPaymentSumByCategory(@Param("statisticType") Integer statisticType,
                                                        @Param("startDate") String startDate,
                                                        @Param("endDate") String endDate,
                                                        @Param("timeType") Integer timeType);

}
