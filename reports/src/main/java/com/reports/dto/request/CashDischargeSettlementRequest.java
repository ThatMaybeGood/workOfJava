package com.reports.dto.request;

import com.reports.dto.common.BaseRequestBody;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 出院结算报表 - 请求体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CashDischargeSettlementRequest extends BaseRequestBody {

    private static final long serialVersionUID = 1L;

    /**
     * 统计维度：month（按月统计）、day（按天统计）
     */
    private String dimension;

    /**
     * 开始日期，格式 yyyy-MM-dd
     */
    private String startDate;

    /**
     * 结束日期，格式 yyyy-MM-dd
     */
    private String endDate;

    /**
     * 当前页码（可选，默认第1页）
     */
    private Integer page;

    /**
     * 每页条数（可选，默认10条）
     */
    private Integer pageSize;

}
