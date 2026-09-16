package com.reports.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.reports.dto.common.BaseRequestBody;
import com.reports.dto.response.outpatient.forecast.WeatherItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * 天气数据维护请求体
 * method: reports.common.weather-maintain
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WeatherMaintainRequest extends BaseRequestBody {

    private static final long serialVersionUID = 1L;

    /**
     * 操作类型：query（查询）、save（保存）、delete（删除）
     */
    private String action;

    /**
     * 天气日期（删除时传入），格式 yyyy-MM-dd
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date weatherDate;

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

    /**
     * 保存的天气列表（save 时传入，按 weatherDate 覆盖保存）
     */
    private List<WeatherItem> list;
}
