package com.mergedata.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 节假日请求体
 */
@Data
public class HolidayRequestBody {

    @NotNull(message = "reportdate不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @JsonProperty("reportdate")
    private LocalDate reportDate;

    @NotNull(message = "query_type不能为空")
    @JsonProperty("query_type")
    private String queryType; //0门诊 1 住院

    @NotNull(message = "total_flag不能为空")
    @JsonProperty("total_flag")
    private String totalFlag; //是否需要汇总 0 不需要 1 需要



    private String extendParams1;
    private String extendParams2;
    private String extendParams3;
}
