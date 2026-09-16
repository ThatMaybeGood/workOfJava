package com.reports.service;

import com.reports.dto.request.DischSettleCntRequest;
import com.reports.dto.response.cash.discharge.settlement.PersonCountItem;

import java.util.List;

/**
 * 出院结算人次统计服务
 */
public interface DischSettleCntService {

    /**
     * 按统计维度查询出院结算人次
     *
     * @param request 查询条件(维度/时间粒度/日期范围)
     * @return 人次统计行
     */
    List<PersonCountItem> queryPersonCount(DischSettleCntRequest request);
}
