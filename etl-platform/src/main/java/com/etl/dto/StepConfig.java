package com.etl.dto;

import com.etl.entity.EtlPipelineStep;
import com.etl.util.JsonUtil;
import lombok.Data;

import java.util.Map;

/**
 * 统一的步骤配置载体，替代原先散落在 EtlTaskConfig 60+ 字段中的配置。
 * 从 EtlPipelineStep 的 source_config / target_config JSON 反序列化得到。
 */
@Data
public class StepConfig {

    // ===== 基础信息 =====
    private Long stepId;
    private Long pipelineId;
    private String stepCode;
    private String stepName;
    private String stepType;
    private String stepSubType;

    // ===== 抽取通用 =====
    private String sourceDsName;
    private String sourceType;

    // DB 抽取
    private String sourceTable;
    private String sourceSql;
    private String sourceView;
    private String sourceProcedure;
    private String sourceParams;

    // HTTP/SOAP 抽取
    private String httpUrl;
    private String httpMethod;
    private String httpHeaders;
    private String httpBody;
    private String httpAuthType;
    private String httpUsername;
    private String httpPassword;
    private String httpToken;
    private String httpResponseType;
    private String httpDataPath;
    private String httpPagination;
    private String httpPageParam;
    private String httpSizeParam;
    private Integer httpPageSize;
    private String httpTotalPath;
    private Integer httpTimeout;
    private String httpEncoding;
    private String soapAction;
    private String soapBinding;
    private String soapNamespace;

    // 文件抽取/加载
    private String filePath;
    private String fileFormat;
    private String fileDelimiter;
    private String fileEncoding;
    private String fileHeader;
    private String fileSheetName;

    // ===== 加载通用 =====
    private String targetDsName;
    private String targetTable;
    private String writeMode;
    private String truncateBefore;
    private Integer batchSize;
    private Integer fetchSize;
    private Integer timeoutSeconds;

    /**
     * 从 EtlPipelineStep 构建 StepConfig。
     * source_config JSON 存放抽取参数，target_config JSON 存放加载参数。
     */
    public static StepConfig fromStep(EtlPipelineStep step) {
        StepConfig config = new StepConfig();
        config.setStepId(step.getId());
        config.setPipelineId(step.getPipelineId());
        config.setStepCode(step.getStepCode());
        config.setStepName(step.getStepName());
        config.setStepType(step.getStepType());
        config.setStepSubType(step.getStepSubType());
        config.setSourceDsName(step.getSourceDsName());
        config.setSourceType(step.getSourceType());
        config.setTargetDsName(step.getTargetDsName());
        config.setWriteMode(step.getWriteMode());
        config.setBatchSize(step.getBatchSize() != null ? step.getBatchSize() : 2000);
        config.setTimeoutSeconds(step.getTimeoutSeconds() != null ? step.getTimeoutSeconds() : 1800);

        // 合并 source_config JSON
        if (step.getSourceConfig() != null && !step.getSourceConfig().isEmpty()) {
            mergeFromJson(step.getSourceConfig(), config);
        }

        // 合并 target_config JSON
        if (step.getTargetConfig() != null && !step.getTargetConfig().isEmpty()) {
            mergeFromJson(step.getTargetConfig(), config);
        }

        return config;
    }

    @SuppressWarnings("unchecked")
    private static void mergeFromJson(String json, StepConfig config) {
        try {
            Map<String, Object> map = JsonUtil.fromJson(json, Map.class);
            if (map == null) return;

            // 抽取配置
            if (map.containsKey("sourceTable")) config.setSourceTable((String) map.get("sourceTable"));
            if (map.containsKey("sourceSql")) config.setSourceSql((String) map.get("sourceSql"));
            if (map.containsKey("sourceView")) config.setSourceView((String) map.get("sourceView"));
            if (map.containsKey("sourceProcedure")) config.setSourceProcedure((String) map.get("sourceProcedure"));
            if (map.containsKey("sourceParams")) config.setSourceParams((String) map.get("sourceParams"));

            // HTTP/SOAP 配置
            if (map.containsKey("httpUrl")) config.setHttpUrl((String) map.get("httpUrl"));
            if (map.containsKey("httpMethod")) config.setHttpMethod((String) map.get("httpMethod"));
            if (map.containsKey("httpHeaders")) config.setHttpHeaders((String) map.get("httpHeaders"));
            if (map.containsKey("httpBody")) config.setHttpBody((String) map.get("httpBody"));
            if (map.containsKey("httpAuthType")) config.setHttpAuthType((String) map.get("httpAuthType"));
            if (map.containsKey("httpUsername")) config.setHttpUsername((String) map.get("httpUsername"));
            if (map.containsKey("httpPassword")) config.setHttpPassword((String) map.get("httpPassword"));
            if (map.containsKey("httpToken")) config.setHttpToken((String) map.get("httpToken"));
            if (map.containsKey("httpResponseType")) config.setHttpResponseType((String) map.get("httpResponseType"));
            if (map.containsKey("httpDataPath")) config.setHttpDataPath((String) map.get("httpDataPath"));
            if (map.containsKey("httpPagination")) config.setHttpPagination((String) map.get("httpPagination"));
            if (map.containsKey("httpPageParam")) config.setHttpPageParam((String) map.get("httpPageParam"));
            if (map.containsKey("httpSizeParam")) config.setHttpSizeParam((String) map.get("httpSizeParam"));
            if (map.containsKey("httpPageSize")) config.setHttpPageSize((Integer) map.get("httpPageSize"));
            if (map.containsKey("httpTimeout")) config.setHttpTimeout((Integer) map.get("httpTimeout"));
            if (map.containsKey("httpEncoding")) config.setHttpEncoding((String) map.get("httpEncoding"));
            if (map.containsKey("soapAction")) config.setSoapAction((String) map.get("soapAction"));
            if (map.containsKey("soapBinding")) config.setSoapBinding((String) map.get("soapBinding"));
            if (map.containsKey("soapNamespace")) config.setSoapNamespace((String) map.get("soapNamespace"));

            // 文件配置
            if (map.containsKey("filePath")) config.setFilePath((String) map.get("filePath"));
            if (map.containsKey("fileFormat")) config.setFileFormat((String) map.get("fileFormat"));
            if (map.containsKey("fileDelimiter")) config.setFileDelimiter((String) map.get("fileDelimiter"));
            if (map.containsKey("fileEncoding")) config.setFileEncoding((String) map.get("fileEncoding"));
            if (map.containsKey("fileHeader")) config.setFileHeader((String) map.get("fileHeader"));
            if (map.containsKey("fileSheetName")) config.setFileSheetName((String) map.get("fileSheetName"));

            // 加载配置
            if (map.containsKey("targetTable")) config.setTargetTable((String) map.get("targetTable"));
            if (map.containsKey("writeMode")) config.setWriteMode((String) map.get("writeMode"));
            if (map.containsKey("truncateBefore")) config.setTruncateBefore((String) map.get("truncateBefore"));
            if (map.containsKey("batchSize")) config.setBatchSize((Integer) map.get("batchSize"));
            if (map.containsKey("timeoutSeconds")) config.setTimeoutSeconds((Integer) map.get("timeoutSeconds"));
        } catch (Exception e) {
            // JSON 解析失败时忽略
        }
    }
}
