package com.reports.dto.request;

import com.reports.dto.common.BaseRequestBody;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 门诊服务质量分析 - 请求体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OutpatientServiceQualityRequest extends BaseRequestBody {

    private static final long serialVersionUID = 1L;

    /**
     * 明细类型：complaint（投诉明细）、praise（表扬明细）
     */
    private String tab;

    /**
     * 统计时间范围
     */
    private String timeRange;

    /**
     * 开始日期，格式 yyyy-MM-dd
     */
    private String startDate;

    /**
     * 结束日期，格式 yyyy-MM-dd
     */
    private String endDate;

    /**
     * 科室名称（可选）
     */
    private String deptName;

    /**
     * 当前页码（可选，默认第1页）
     */
    private Integer page;

    /**
     * 每页条数（可选，默认10条）
     */
    private Integer pageSize;

}
