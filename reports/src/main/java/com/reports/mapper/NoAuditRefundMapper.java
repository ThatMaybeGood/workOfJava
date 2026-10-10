package com.reports.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 无审退费 Mapper（yq_powercfp 库，t_t_refund_apply：is_return=1 按 TRUNC(apply_time) 归期）
 */
@Mapper
public interface NoAuditRefundMapper {

    /** bt14 无审退费张数：指定日期区间的无审退费收据张数 */
    List<Map<String, Object>> queryNoAuditRefundCount(@Param("startDate") Date startDate,
                                                      @Param("endDate") Date endDate);

    /** bt15 无审退费金额：指定日期区间的无审退费金额 */
    List<Map<String, Object>> queryNoAuditRefundAmount(@Param("startDate") Date startDate,
                                                       @Param("endDate") Date endDate);
}
