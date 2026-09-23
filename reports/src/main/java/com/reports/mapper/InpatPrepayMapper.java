package com.reports.mapper;

import com.reports.entity.InpatPrepayOvEntity;
import com.reports.entity.InpatPrepayDtlEntity;
import com.reports.entity.InpatPrepayChtEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 住院预交金统计 Mapper(从交易流水表 tr_inpat_prepay_rcpt 实时聚合)
 */
@Mapper
public interface InpatPrepayMapper {

    /**
     * 概览聚合
     *
     * @param startDate    本期开始日期
     * @param endDate      本期结束日期
     * @param lastStartDate 同期(去年)开始日期
     * @param lastEndDate   同期(去年)结束日期
     * @param scope         数据范围(SUMMARY/INCOME/REFUND)
     * @return 概览聚合
     */
    InpatPrepayOvEntity queryOverview(@Param("startDate") Date startDate,
                                      @Param("endDate") Date endDate,
                                      @Param("lastStartDate") Date lastStartDate,
                                      @Param("lastEndDate") Date lastEndDate,
                                      @Param("scope") String scope);

    /**
     * 按天聚合
     *
     * @param startDate    本期开始日期
     * @param endDate      本期结束日期
     * @param lastStartDate 同期(去年)开始日期
     * @param lastEndDate   同期(去年)结束日期
     * @param scope         数据范围(SUMMARY/INCOME/REFUND)
     * @param month         true按月聚合(返回每月一行),false按天
     * @return 按天/按月聚合
     */
    List<InpatPrepayDtlEntity> queryDaily(@Param("startDate") Date startDate,
                                          @Param("endDate") Date endDate,
                                          @Param("lastStartDate") Date lastStartDate,
                                          @Param("lastEndDate") Date lastEndDate,
                                          @Param("scope") String scope,
                                          @Param("month") boolean month);

    /**
     * 渠道×支付方式聚合
     *
     * @param startDate    本期开始日期
     * @param endDate      本期结束日期
     * @param lastStartDate 同期(去年)开始日期
     * @param lastEndDate   同期(去年)结束日期
     * @param scope         数据范围(SUMMARY/INCOME/REFUND)
     * @return 渠道×支付方式聚合
     */
    List<InpatPrepayChtEntity> queryChannel(@Param("startDate") Date startDate,
                                            @Param("endDate") Date endDate,
                                            @Param("lastStartDate") Date lastStartDate,
                                            @Param("lastEndDate") Date lastEndDate,
                                            @Param("scope") String scope);
}
