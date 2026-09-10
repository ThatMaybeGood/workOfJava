package com.reports.dto.request;

import com.reports.dto.common.BaseRequestBody;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 门诊预警统计 - 请求体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OutpatientAlertRequest extends BaseRequestBody {

    private static final long serialVersionUID = 1L;

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
     * 科室编码（可选）
     */
    private String deptCode;

    /**
     * 当前页码（可选，默认第1页）
     */
    private Integer page;

    /**
     * 每页条数（可选，默认10条）
     */
    private Integer pageSize;

    /**
     * 是否查询明细（true = 返回明细列表，不走分页；与 page/pageSize 互斥）
     */
    private Boolean detail;

    /**
     * 医生姓名（明细筛选，可选）
     */
    private String doctorName;

    /**
     * 出诊时间段（明细筛选，可选，模糊匹配）
     */
    private String clinicPeriod;

}
