package com.mergedata.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 入参：根据日期查询门诊报表数据
 */
@Data
public class OutpReportRequestBody {

    @NotNull(message = "reportdate不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @JsonProperty("reportdate")
    private LocalDate reportDate;

    /**
     * 入参：窗口号
     */
    @JsonProperty("inp_window")
    private Integer inpWindow;

    /**
     * 入参：ATM号
     */
    @JsonProperty("atm")
    private Integer atm;

    private String extendParams1;
    private String extendParams2;
    private String extendParams3;

}
