package com.reports.service.impl;

import com.reports.annotation.DataSource;
import com.reports.mapper.NoAuditRefundMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 无审退费-源库读取（yq_powercfp）
 * <p>
 * 单独一个 bean 挂 {@code @DataSource("yq_powercfp")}：切库是方法级 AOP，
 * 必须和查 master 库的逻辑分在两个 bean 里，否则切不过来。
 */
@Slf4j
@Service
@DataSource("yq_powercfp")
public class NoAuditRefundReader {

    private final NoAuditRefundMapper noAuditRefundMapper;

    @Autowired
    public NoAuditRefundReader(NoAuditRefundMapper noAuditRefundMapper) {
        this.noAuditRefundMapper = noAuditRefundMapper;
    }

    /** bt14 无审退费张数，源库读失败按 0 返回 */
    public List<Map<String, Object>> queryNoAuditCount(Date startDate, Date endDate) {
        try {
            return noAuditRefundMapper.queryNoAuditRefundCount(startDate, endDate);
        } catch (Exception e) {
            log.warn("读取无审退费张数失败", e);
            return zeroRow("cnt");
        }
    }

    /** bt15 无审退费金额，源库读失败按 0 返回 */
    public List<Map<String, Object>> queryNoAuditAmount(Date startDate, Date endDate) {
        try {
            return noAuditRefundMapper.queryNoAuditRefundAmount(startDate, endDate);
        } catch (Exception e) {
            log.warn("读取无审退费金额失败", e);
            return zeroRow("amount");
        }
    }

    private List<Map<String, Object>> zeroRow(String valueKey) {
        Map<String, Object> row = new HashMap<>();
        row.put("name", "无审退费");
        row.put(valueKey, 0);
        return List.of(row);
    }
}
