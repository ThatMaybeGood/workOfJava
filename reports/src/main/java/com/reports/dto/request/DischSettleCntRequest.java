package com.reports.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.reports.dto.common.BaseRequestBody;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 出院结算人次统计请求体
 * method: reports.cash.disch-settle-cnt
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DischSettleCntRequest extends BaseRequestBody {

    private static final long serialVersionUID = 1L;

    /**
     * 统计维度：summary（按费别汇总）/ operator（按操作员）/ payType（按支付类别）
     */
    private String dimension;

    /**
     * 时间粒度：day（按天）/ month（按月）
     */
    private String timeDimension;

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
}
