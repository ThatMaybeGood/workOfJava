package com.mergedata.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Data
public class ReportRequestBody {

    @NotBlank(message = "reportdate不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @JsonProperty("reportdate")
    private LocalDate reportDate;

    private String extendParams1;

    private String extendParams2;
    private String extendParams3;

    @JsonProperty("inp_window")
    private Integer inpWindow;

    @JsonProperty("atm")
    private Integer ATM;
}
