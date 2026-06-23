package com.reports.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.reports.dto.common.BaseRequestBody;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 门诊运行数据统计 - 请求体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OutpatientOperationRequest extends BaseRequestBody {

    private static final long serialVersionUID = 1L;

    /**
     * 开始日期，格式 yyyy-MM-dd
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startDate;

    /**
     * 结束日期，格式 yyyy-MM-dd
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endDate;

    /**
     * 科室编码
     */
    private String deptCode;

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
