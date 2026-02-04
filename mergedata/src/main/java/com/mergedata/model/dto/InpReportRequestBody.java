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

    /**
     * 初始化标志 0 正常 1 初始化
     */
    private String initFlag;

    /**
     * 节假日汇总标志 0正常 1汇总
     */
    private String holidayTotalFlag;

    private String extendParams1;
    private String extendParams2;
    private String extendParams3;

}
