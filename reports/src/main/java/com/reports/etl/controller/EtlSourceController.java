package com.reports.etl.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.dto.common.ApiResponse;
import com.reports.etl.entity.EtlProcConfig;
import com.reports.etl.entity.EtlSource;
import com.reports.etl.entity.EtlWsConfig;
import com.reports.etl.service.core.EtlMetaDao;
import com.reports.etl.service.core.EtlStructureService;
import com.reports.etl.service.extractor.ProcedureExtractor;
import com.reports.etl.service.extractor.SourceExtractorFacade;
import com.reports.etl.service.extractor.WebServiceExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 抽取来源（Source）管理：独立实体，可被多个任务复用
 */
@Slf4j
@RestController
@RequestMapping("/api/etl/source")
public class EtlSourceController {

    private final EtlMetaDao metaDao;
    private final SourceExtractorFacade sourceExtractorFacade;
    private final WebServiceExtractor webServiceExtractor;
    private final ProcedureExtractor procedureExtractor;
    private final EtlStructureService structureService;

    public EtlSourceController(EtlMetaDao metaDao,
                               SourceExtractorFacade sourceExtractorFacade,
                               WebServiceExtractor webServiceExtractor,
                               ProcedureExtractor procedureExtractor,
                               EtlStructureService structureService) {
        this.metaDao = metaDao;
        this.sourceExtractorFacade = sourceExtractorFacade;
        this.webServiceExtractor = webServiceExtractor;
        this.procedureExtractor = procedureExtractor;
        this.structureService = structureService;
    }

    @GetMapping("/list")
    public ApiResponse<?> list() {
        List<EtlSource> all = metaDao.listSources();
        Map<String, Object> wrap = new LinkedHashMap<>();
        wrap.put("records", all);
        wrap.put("total", all.size());
        return ApiResponse.success(wrap);
    }

    @GetMapping("/{id}")
    public ApiResponse<EtlSource> get(@PathVariable Long id) {
        return ApiResponse.success(metaDao.getSource(id));
    }

    @PostMapping
    public ApiResponse<Long> add(@RequestBody EtlSource source) {
        Long id = metaDao.insertSource(source);
        return ApiResponse.success(id);
    }

    @PutMapping("/{id}")
    public ApiResponse<String> update(@PathVariable Long id, @RequestBody EtlSource source) {
        source.setId(id);
        metaDao.updateSource(source);
        return ApiResponse.success("来源更新成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        int refCount = metaDao.countTasksBySourceId(id);
        if (refCount > 0) {
            return ApiResponse.fail("400", "来源删除失败", "SOURCE_REFERENCED",
                    "该来源正被 " + refCount + " 个任务引用，请先解除任务关联");
        }
        metaDao.deleteSource(id);
        return ApiResponse.success("来源删除成功");
    }

    /**
     * 未保存来源配置的预览调试（不落库）
     * body: { type, sourceDsId, ...配置字段 }
     */
    /**
     * 未保存来源配置的预览调试（不落库）
     * body 两种兼容格式：
     * 1. 原始配置字段集：{ type, sourceDsId, url, wsType, ... }
     * 2. 完整来源记录：{ id, name, type, sourceDsId, configJson: '{...}' }
     * 第 2 种时自动展开 configJson
     */
    @PostMapping("/preview-debug")
    public ApiResponse<Map<String, Object>> previewDebug(@RequestBody Map<String, Object> rawConfig) {
        long start = System.currentTimeMillis();
        // 兼容前端直接传完整 source 记录的情况
        if (rawConfig != null && rawConfig.containsKey("configJson")) {
            try {
                Map<String, Object> cfg = new ObjectMapper().readValue(
                        rawConfig.get("configJson").toString(), Map.class);
                if (rawConfig.get("type") != null) cfg.put("type", rawConfig.get("type"));
                if (rawConfig.get("sourceDsId") != null) cfg.put("sourceDsId", rawConfig.get("sourceDsId"));
                rawConfig = cfg;
            } catch (Exception e) {
                log.warn("来源配置 JSON 解析失败: {}", e.getMessage());
            }
        }
        try {
            Map<String, Object> extractResult = sourceExtractorFacade.extractPreview(rawConfig, 20);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("columns", extractResult.get("columns"));
            result.put("rows", extractResult.get("rows"));
            result.put("totalRows", extractResult.get("totalRows"));
            result.put("durationMs", System.currentTimeMillis() - start);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.warn("来源预览调试失败: {}", e.getMessage());
            return ApiResponse.fail("500", "来源预览调试失败", "PREVIEW_DEBUG_ERROR",
                    e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
        }
    }

    /**
     * 来源结构树：实际调用来源取样例行，递归展开层级
     * WS：基于原始 JSON/XML 响应（保留嵌套）；PROC：游标 ResultSetMetaData 平铺
     */
    @GetMapping("/structure/{id}")
    public ApiResponse<Map<String, Object>> structure(@PathVariable Long id) {
        EtlSource source = metaDao.getSource(id);
        if (source == null) {
            return ApiResponse.fail("404", "来源不存在", "404", "sourceId=" + id);
        }
        try {
            List<Map<String, Object>> tree;
            if ("WS".equals(source.getType())) {
                EtlWsConfig cfg = sourceExtractorFacade.toWsConfig(source);
                List<JsonNode> samples = webServiceExtractor.fetchRawSample(cfg, 50);
                tree = structureService.buildTreeFromJson(samples);
            } else if ("PROC".equals(source.getType())) {
                EtlProcConfig cfg = sourceExtractorFacade.toProcConfig(source);
                List<Map<String, Object>> columnsMeta =
                        procedureExtractor.describeColumns(cfg, source.getSourceDsId());
                tree = structureService.buildFlatTree(columnsMeta);
            } else {
                return ApiResponse.fail("400", "未知来源类型", "400", "type=" + source.getType());
            }
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("tree", tree);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.warn("来源结构分析失败 sourceId={}: {}", id, e.getMessage());
            return ApiResponse.fail("500", "来源结构分析失败", "STRUCTURE_ERROR",
                    e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
        }
    }
}
