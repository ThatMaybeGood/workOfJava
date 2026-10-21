package com.reports.service;

import com.reports.dto.request.PreHospTransRequest;
import com.reports.dto.response.cash.prehosp.PreHospTransItem;

import java.util.List;

/**
 * 院前转住院统计服务
 */
public interface PreHospTransService {

    /**
     * 统计查询: 只统计维护的有效操作员, 姓名从HIS users回显
     */
    List<PreHospTransItem> queryStats(PreHospTransRequest request);
}
