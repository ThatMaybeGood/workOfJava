package com.reports.dto.request;

import com.reports.dto.common.BaseRequestBody;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

/**
 * 收费员结账统计 - 请求体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CashCashierSettlementRequest extends BaseRequestBody {

    private static final long serialVersionUID = 1L;

    /**
     * 统计页签：cashier（按收费员统计）、source（按来源方式统计）、workload（工作量报表）
     */
    private String tab;

    /**
     * 统计维度：month（按月统计）、day（按天统计）
     */
    private String dimension;

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
     * 当前页码（可选，默认第1页）
     */
    private Integer page;

    /**
     * 每页条数（可选，默认10条）
     */
    private Integer pageSize;

}
