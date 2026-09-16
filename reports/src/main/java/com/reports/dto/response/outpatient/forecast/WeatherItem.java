package com.reports.dto.response.outpatient.forecast;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 天气数据维护明细项
 */
@Data
public class WeatherItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 天气日期，格式 yyyy-MM-dd
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date weatherDate;

    /**
     * 天气类型(晴/阴/雨/雪等)
     */
    private String weatherType;

    /**
     * 天气出勤系数(空按1处理)
     */
    private Double weatherCoef;

    /**
     * 来源(人工登记/接口同步)
     */
    private String weatherSource;
}
