package com.reports.service.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;
import com.reports.dto.request.MiReconRequest;
import com.reports.dto.response.cash.mi.MiReconCompareItem;
import com.reports.dto.response.cash.mi.MiReconFileItem;
import com.reports.dto.response.cash.mi.MiReconTableTotalItem;
import com.reports.dto.response.cash.mi.MiReconUnmatchedNegItem;
import com.reports.dto.response.cash.mi.MiReconZpDiffItem;
import com.reports.enums.ReportModule;
import com.reports.service.MiReconService;
import com.reports.service.handler.MethodMapping;
import com.reports.service.handler.ReportHandler;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 医保对账每日对比处理器
 * method: reports.cash.mi-recon
 * 文件上传走 MiReconUploadController(/reports/mi-recon/upload)
 */
@Slf4j
@Component
@MethodMapping("reports.cash.mi-recon")
public class MiReconHandler implements ReportHandler<MiReconRequest, Map<String, Object>> {

    private final MiReconService miReconService;
    private final ObjectMapper objectMapper;

    @Autowired
    public MiReconHandler(MiReconService miReconService, ObjectMapper objectMapper) {
        this.miReconService = miReconService;
        this.objectMapper = objectMapper;
    }

    private static final ReportModule MODULE = ReportModule.MI_RECON;

    @Override
    public ApiResponse<Map<String, Object>> handle(ApiRequest<Object> request) {
        SeqUtil.next();
        log.info("[{}] 处理请求", MODULE.getChineseName());

        MiReconRequest body;
        if (request.getBody() instanceof MiReconRequest) {
            body = (MiReconRequest) request.getBody();
        } else {
            body = objectMapper.convertValue(request.getBody(), MiReconRequest.class);
        }
        if (body == null) {
            body = new MiReconRequest();
        }

        String action = body.getAction() == null ? "compare" : body.getAction();
        Map<String, Object> result = new HashMap<>();
        switch (action) {
            case "records": {
                List<MiReconFileItem> list = miReconService.queryRecords();
                result.put("list", list);
                result.put("total", list.size());
                return ApiResponse.success(result, "上传记录查询成功！");
            }
            case "updateType": {
                int affected = miReconService.updateType(body.getId(), body.getInsurType());
                result.put("affected", affected);
                return ApiResponse.success(result, affected > 0 ? "类型修改成功！" : "记录不存在，修改失败！");
            }
            case "enableFile": {
                int affected = miReconService.enableFile(body.getId());
                return ApiResponse.success(result, affected > 0 ? "启用成功，该文件数据已用于对比！" : "该记录已是启用状态！");
            }
            case "zpDiff": {
                List<MiReconZpDiffItem> list = miReconService.queryZpDiff(body.getInsurType(), body.getSettleDate());
                List<MiReconTableTotalItem> totals = miReconService.queryDayTotals(body.getInsurType(), body.getSettleDate());
                result.put("list", list);
                result.put("total", list.size());
                result.put("totals", totals);
                return ApiResponse.success(result, "账平差异查询成功！");
            }
            case "unmatchedNeg": {
                List<MiReconUnmatchedNegItem> list =
                        miReconService.queryUnmatchedNeg(body.getInsurType(), body.getSettleDate());
                result.put("list", list);
                result.put("total", list.size());
                return ApiResponse.success(result, "未匹配负交易查询成功！");
            }
            default: {
                List<MiReconCompareItem> list = miReconService.queryCompare(body);
                result.put("list", list);
                result.put("total", list.size());
                return ApiResponse.success(result, "医保对账对比查询成功！");
            }
        }
    }
}
