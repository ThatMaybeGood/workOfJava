package com.reports.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 住院预交金统计请求
 *
 * <p>页面有多个子接口（概览 / 汇总表 / 进项表 / 退项表 / 趋势图 / 渠道图 / 支付方式图），
 * 走同一个 method，靠 {@code RequestHead.endpoint} 区分。
 */
@Data
public class InpatPrepayRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 维度：day / month */
    private String dimension;

    /**
     * 开始日期，格式 yyyy-MM-dd
     * <p>必须和 cash 下其它请求 DTO 一样带上 @JsonFormat：不加的话 Jackson 会把
     * "yyyy-MM-dd" 当 UTC 解析，绑到 Oracle 时变成当天 08:00，区间首日会被整行排除。
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startDate;

    /** 结束日期，格式 yyyy-MM-dd */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endDate;

    /** 图表口径：summary_count / summary_amount / income_count / income_amount / refund_* */
    private String type;

    /** 页码 */
    private Integer page;

    /** 每页条数 */
    private Integer pageSize;
}
