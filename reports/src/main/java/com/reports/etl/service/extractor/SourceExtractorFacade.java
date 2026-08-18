package com.reports.etl.service.extractor;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.etl.entity.EtlProcConfig;
import com.reports.etl.entity.EtlSource;
import com.reports.etl.entity.EtlWsConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 抽取来源门面：按来源类型解析 configJson，委托给具体 Extractor。
 * 任务执行、调试、预览、结构分析统一从这里进入。
 */
@Slf4j
@Component
public class SourceExtractorFacade {

    private final WebServiceExtractor webServiceExtractor;
    private final ProcedureExtractor procedureExtractor;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public SourceExtractorFacade(WebServiceExtractor webServiceExtractor,
                                 ProcedureExtractor procedureExtractor) {
        this.webServiceExtractor = webServiceExtractor;
        this.procedureExtractor = procedureExtractor;
    }

    /**
     * 按来源配置抽取一批数据（行为与旧 taskId 路径一致）
     */
    public Map<String, Object> extract(EtlSource source, int batchSize) {
        if (source == null) throw new RuntimeException("抽取来源不存在");
        if ("WS".equals(source.getType())) {
            return webServiceExtractor.extract(toWsConfig(source), batchSize);
        } else if ("PROC".equals(source.getType())) {
            return procedureExtractor.extract(toProcConfig(source), source.getSourceDsId(), batchSize);
        }
        throw new RuntimeException("未知来源类型: " + source.getType());
    }

    /**
     * 未保存配置的预览调试（body 即配置字段集，不落库）
     */
    public Map<String, Object> extractPreview(Map<String, Object> rawConfig, int batchSize) {
        Object type = rawConfig.get("type");
        Long sourceDsId = rawConfig.get("sourceDsId") != null
                ? Long.valueOf(rawConfig.get("sourceDsId").toString()) : null;
        if ("WS".equals(type)) {
            EtlWsConfig cfg = objectMapper.convertValue(rawConfig, EtlWsConfig.class);
            return webServiceExtractor.extract(cfg, batchSize);
        } else if ("PROC".equals(type)) {
            EtlProcConfig cfg = objectMapper.convertValue(rawConfig, EtlProcConfig.class);
            return procedureExtractor.extract(cfg, sourceDsId, batchSize);
        }
        throw new RuntimeException("未知来源类型: " + type);
    }

    public EtlWsConfig toWsConfig(EtlSource source) {
        try {
            return objectMapper.readValue(source.getConfigJson(), EtlWsConfig.class);
        } catch (Exception e) {
            throw new RuntimeException("来源配置解析失败(WS): " + e.getMessage(), e);
        }
    }

    public EtlProcConfig toProcConfig(EtlSource source) {
        try {
            return objectMapper.readValue(source.getConfigJson(), EtlProcConfig.class);
        } catch (Exception e) {
            throw new RuntimeException("来源配置解析失败(PROC): " + e.getMessage(), e);
        }
    }
}
