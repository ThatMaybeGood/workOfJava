package com.reports.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.reports.dto.common.BaseRequestBody;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 运营数据统计 - 请求体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StatDataSummaryRequest extends BaseRequestBody {

    private static final long serialVersionUID = 1L;

    /**
     * 统计页签(13个): special-express / no-accept-long / no-accept-24h / doctor-response
     * / rx-audit-time / clinic-unit / patient-eval / cancel-reason / return-no-reg
     * / pay-return / dept-wait-time / appt-success / recheck-appt
     */
    private String tab;

    /**
     * 统计维度：month(按月)、day(按天)，仅前端控件用，后端按 startDate/endDate 圈日期
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
     * 当前页码(仅 cancel-reason / return-no-reg 明细页签用)
     */
    private Integer page;

    /**
     * 每页条数(仅 cancel-reason / return-no-reg 明细页签用)
     */
    private Integer pageSize;

}
