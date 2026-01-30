package com.mergedata.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 入参：根据日期查询住院报表数据
 */
@Data
public class InpReportRequestBody {

    @NotNull(message = "reportdate不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @JsonProperty("reportdate")
    private LocalDate reportDate;

    private String extendParams1;
    private String extendParams2;
    private String extendParams3;

}
