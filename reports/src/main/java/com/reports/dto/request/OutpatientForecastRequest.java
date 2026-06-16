package com.reports.dto.request;

import com.reports.dto.common.BaseRequestBody;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 预测门诊量报表 - 请求体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OutpatientForecastRequest extends BaseRequestBody {

    private static final long serialVersionUID = 1L;

    /**
     * 科室名称（可选，空字符串表示全部）
     */
    private String deptName;

}
