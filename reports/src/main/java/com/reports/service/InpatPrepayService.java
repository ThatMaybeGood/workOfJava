package com.reports.service;

import com.reports.dto.request.InpatPrepayRequest;

import java.util.Map;

/**
 * 住院预交金统计服务
 *
 * <p>每个方法对应页面上的一个子接口，返回结构直接按前端读取的字段拼装的 Map，
 * 不再为每种图形单独定义 DTO。
 */
public interface InpatPrepayService {

    /** 概览指标（扁平对象） */
    Map<String, Object> queryOverview(InpatPrepayRequest request);

    /**
     * 明细表（汇总 / 进项 / 退项）
     *
     * @param dataType SUMMARY / INCOME / REFUND
     */
    Map<String, Object> queryTable(InpatPrepayRequest request, String dataType);

    /** 趋势图（本期 vs 上期） */
    Map<String, Object> queryTrendChart(InpatPrepayRequest request);

    /** 渠道分析（饼图 x2 + 渠道×支付方式堆叠图） */
    Map<String, Object> queryChannelChart(InpatPrepayRequest request);

    /** 支付方式分析（退项用） */
    Map<String, Object> queryPayTypeChart(InpatPrepayRequest request);
}
