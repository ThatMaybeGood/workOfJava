package com.reports.dto.request;

import com.reports.dto.common.BaseRequestBody;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 互医质控运营月报 - 请求体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OutpatientInternetHospitalRequest extends BaseRequestBody {

    private static final long serialVersionUID = 1L;

    /**
     * 统计月份，格式 yyyy-MM
     */
    private String month;

    /**
     * 当前页码（可选，默认第1页）
     */
    private Integer page;

    /**
     * 每页条数（可选，默认10条）
     */
    private Integer pageSize;

}
