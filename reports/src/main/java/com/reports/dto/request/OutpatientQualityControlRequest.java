package com.reports.dto.request;

import com.reports.dto.common.BaseRequestBody;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 门诊管理质量控制 - 请求体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OutpatientQualityControlRequest extends BaseRequestBody {

    private static final long serialVersionUID = 1L;

    /**
     * 开始月份，格式 yyyy-MM
     */
    private String startMonth;

    /**
     * 结束月份，格式 yyyy-MM
     */
    private String endMonth;

    /**
     * 当前页码（可选，默认第1页）
     */
    private Integer page;

    /**
     * 每页条数（可选，默认10条）
     */
    private Integer pageSize;

}
