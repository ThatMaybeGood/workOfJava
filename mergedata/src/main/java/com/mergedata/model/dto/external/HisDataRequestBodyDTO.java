package com.mergedata.model.dto.external;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class HisDataRequestBodyDTO {

    @NotBlank(message = "reportdate不能为空")
    private String reportdate;
    private String extendParams1;
    private String extendParams2;
    private String extendParams3;
}
