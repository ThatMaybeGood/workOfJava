package com.etl.dto;

import lombok.Data;

import java.util.List;

/**
 * 一次调试运行的完整结果，包含每个步骤的执行结果与分流信息。
 */
@Data
public class DebugResult {

    /** 任务编码 */
    private String taskCode;

    /** 任务名称 */
    private String taskName;

    /** 本次调试执行ID */
    private String executionId;

    /** 总体状态: SUCCESS / FAILED */
    private String status;

    /** 总体耗时(毫秒) */
    private long totalDurationMs;

    /** 各步骤执行结果 */
    private List<StepResult> steps;

    /** 错误信息（任一环节失败时） */
    private String errorMessage;
}
