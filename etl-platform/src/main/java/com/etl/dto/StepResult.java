package com.etl.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 单个执行步骤的结果，用于步骤编排调试与可视化。
 */
@Data
public class StepResult {

    /** 步骤序号（从1开始） */
    private int stepOrder;

    /** 步骤类型: EXTRACT / TRANSFORM / LOAD */
    private String stepType;

    /** 步骤名称 */
    private String stepName;

    /** 执行状态: SUCCESS / FAILED / SKIPPED */
    private String status;

    /** 输入行数 */
    private long inputRows;

    /** 输出行数 */
    private long outputRows;

    /** 耗时(毫秒) */
    private long durationMs;

    /** 出参数据预览（限制条数） */
    private List<Map<String, Object>> outputData;

    /** 字段结构（列名列表） */
    private List<String> outputColumns;

    /** 错误信息 */
    private String errorMessage;

    /** 分流标记：成功后是否继续下一步 */
    private boolean nextOnSuccess;

    /** 分流标记：失败后是否跳过（继续下一步） */
    private boolean nextOnFail;
}
