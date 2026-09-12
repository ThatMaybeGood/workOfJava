package com.reports.service.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;
import com.reports.dto.request.InpatPrepayRequest;
import com.reports.service.InpatPrepayService;
import com.reports.service.handler.MethodMapping;
import com.reports.service.handler.ReportHandler;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 住院预交金统计处理器。
 *
 * <p>页面上的 7 个子接口走的是同一个 method，靠 {@code RequestHead.endpoint} 区分：
 * overview / summaryTable / incomeTable / refundTable / trendChart / channelChart / payTypeChart。
 * 每个子接口返回前端直接读取的那一层结构（概览是扁平对象、表格是 {list,total,...}、
 * 图表是各自需要的系列），而不是把它们合并成一个大对象——前端是按子接口分别取数的。
 */
@Slf4j
@Component
@MethodMapping("reports.cash.cash-inpatient-prepayment")
public class InpatPrepayHandler implements ReportHandler<InpatPrepayRequest, Map<String, Object>> {

    private static final String MODULE_NAME = "住院预交金统计";

    private final InpatPrepayService inpatPrepayService;
    private final ObjectMapper objectMapper;

    @Autowired
    public InpatPrepayHandler(InpatPrepayService inpatPrepayService, ObjectMapper objectMapper) {
        this.inpatPrepayService = inpatPrepayService;
        this.objectMapper = objectMapper;
    }

    @Override
    public ApiResponse<Map<String, Object>> handle(ApiRequest<Object> request) {
        SeqUtil.next();
        log.info("[{}] 处理请求", MODULE_NAME);

        InpatPrepayRequest body;
        if (request.getBody() instanceof InpatPrepayRequest) {
            body = (InpatPrepayRequest) request.getBody();
        } else {
            body = objectMapper.convertValue(request.getBody(), InpatPrepayRequest.class);
        }
        if (body == null) {
            body = new InpatPrepayRequest();
        }

        String endpoint = request.getHead() == null ? null : request.getHead().getEndpoint();
        log.info("[{}] 子接口 endpoint={}", MODULE_NAME, endpoint);

        Map<String, Object> result;
        if ("summaryTable".equals(endpoint)) {
            result = inpatPrepayService.queryTable(body, "SUMMARY");
        } else if ("incomeTable".equals(endpoint)) {
            result = inpatPrepayService.queryTable(body, "INCOME");
        } else if ("refundTable".equals(endpoint)) {
            result = inpatPrepayService.queryTable(body, "REFUND");
        } else if ("trendChart".equals(endpoint)) {
            result = inpatPrepayService.queryTrendChart(body);
        } else if ("channelChart".equals(endpoint)) {
            result = inpatPrepayService.queryChannelChart(body);
        } else if ("payTypeChart".equals(endpoint)) {
            result = inpatPrepayService.queryPayTypeChart(body);
        } else {
            result = inpatPrepayService.queryOverview(body);
        }
        return ApiResponse.success(result, MODULE_NAME + "查询成功！");
    }
}
