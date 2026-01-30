package com.mergedata.model.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class HisDataRequestBodyDTO {

    /*
    报表日期
     */
    @NotBlank(message = "reportdate不能为空")
    @JsonProperty("reportdate")
    private String reportDate;

    /*
    扩展参数1
     */
    private String extendParams1;

    /*
    扩展参数2
     */
    private String extendParams2;

    /*
    扩展参数3
     */
    private String extendParams3;
}
