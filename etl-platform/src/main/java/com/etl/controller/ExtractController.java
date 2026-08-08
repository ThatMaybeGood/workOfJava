package com.etl.controller;

import com.etl.dto.ApiResponse;
import com.etl.dto.ExtractTestRequest;
import com.etl.dto.ExtractTestResult;
import com.etl.entity.EtlTaskConfig;
import com.etl.service.core.DataSourceManager;
import com.etl.service.reader.DataSourceReader;
import com.etl.service.reader.ReaderFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 抽取调试控制器 —— 独立于任务管理，支持在不保存任务的情况下测试抽取配置。
 */
@Slf4j
@RestController
@RequestMapping("/api/etl/extract")
@Tag(name = "抽取调试", description = "抽取源配置测试和调试")
public class ExtractController {

    @Autowired
    private ReaderFactory readerFactory;

    @Autowired
    private DataSourceManager dataSourceManager;

    @PostMapping("/test")
    @Operation(summary = "测试抽取配置", description = "传入抽取配置（不需要保存任务），返回原始响应和解析后的数据")
    public ApiResponse<ExtractTestResult> testExtract(@RequestBody ExtractTestRequest req) {
        long start = System.currentTimeMillis();

        try {
            // 构建临时 EtlTaskConfig
            EtlTaskConfig task = buildTempTask(req);

            // 获取 Reader
            DataSourceReader reader = readerFactory.getReader(req.getSourceType());

            // 初始化
            reader.init(task, dataSourceManager);

            // 读取数据
            int limit = req.getLimit() != null && req.getLimit() > 0 ? req.getLimit() : 50;
            List<Map<String, Object>> data = reader.preview(limit);
            long duration = System.currentTimeMillis() - start;

            // 构建结果
            ExtractTestResult result = ExtractTestResult.ok(null, req.getResponseType(), data, duration);
            // 对于 HTTP/SOAP，不在这里获取 raw response —— Reader 内部已处理
            // raw response 需要 Reader 返回，目前简化处理
            result.setFinalUrl(req.getUrl());
            result.setFinalMethod(req.getHttpMethod() != null ? req.getHttpMethod() : "GET");

            reader.close();
            return ApiResponse.success(result, "抽取测试成功, 返回 " + result.getTotalRows() + " 条数据");

        } catch (Exception e) {
            log.error("抽取测试失败", e);
            long duration = System.currentTimeMillis() - start;
            ExtractTestResult failResult = ExtractTestResult.fail(e.getMessage());
            failResult.setDurationMs(duration);
            return ApiResponse.success(failResult); // 用 success 包装，前端由 success 字段判断
        }
    }

    @PostMapping("/raw")
    @Operation(summary = "获取原始响应", description = "仅返回 HTTP/SOAP 请求的原始响应体，不做解析")
    public ApiResponse<String> rawResponse(@RequestBody ExtractTestRequest req) {
        try {
            EtlTaskConfig task = buildTempTask(req);
            DataSourceReader reader = readerFactory.getReader(req.getSourceType());
            reader.init(task, dataSourceManager);

            List<Map<String, Object>> data = reader.preview(req.getLimit() != null ? req.getLimit() : 10);
            reader.close();

            // 将原始数据序列化返回
            StringBuilder sb = new StringBuilder();
            if (data != null) {
                for (Map<String, Object> row : data) {
                    sb.append(row.toString()).append("\n");
                }
            }
            return ApiResponse.success(sb.toString(), "原始数据获取成功");

        } catch (Exception e) {
            log.error("获取原始响应失败", e);
            return ApiResponse.error("获取原始响应失败: " + e.getMessage());
        }
    }

    /**
     * 从测试请求构建临时任务配置
     */
    private EtlTaskConfig buildTempTask(ExtractTestRequest req) {
        EtlTaskConfig t = new EtlTaskConfig();
        t.setSourceType(req.getSourceType());
        t.setSourceDsName(req.getDataSourceName());
        t.setSourceTable(req.getTableName());
        t.setSourceProcedure(req.getProcedureName());
        t.setSourceSql(req.getSqlText());
        t.setSourceView(req.getViewName());
        t.setSourceParams(req.getSourceParams());
        t.setHttpUrl(req.getUrl());
        t.setHttpMethod(req.getHttpMethod() != null ? req.getHttpMethod() : "GET");
        t.setHttpHeaders(req.getHeaders());
        t.setHttpBody(req.getRequestBody());
        t.setHttpAuthType(req.getAuthType());
        t.setHttpUsername(req.getAuthUsername());
        t.setHttpPassword(req.getAuthPassword());
        t.setHttpToken(req.getAuthToken());
        t.setHttpResponseType(req.getResponseType() != null ? req.getResponseType() : "JSON");
        t.setHttpDataPath(req.getDataPath());
        t.setHttpPagination(req.getPagination());
        t.setHttpPageParam(req.getPageParam());
        t.setHttpSizeParam(req.getSizeParam());
        t.setHttpPageSize(req.getPageSize());
        t.setHttpTimeout(req.getTimeout() != null ? req.getTimeout() : 30000);
        t.setSoapAction(req.getSoapAction());
        t.setSoapBinding(req.getSoapBinding());
        t.setSoapNamespace(req.getSoapNamespace());
        return t;
    }
}
