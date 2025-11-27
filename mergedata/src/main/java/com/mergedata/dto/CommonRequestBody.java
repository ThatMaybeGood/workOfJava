package com.mergedata.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CommonRequestBody {
    private String reportdate;
    private String extendParams1;
    private String extendParams2;
    private String extendParams3;
}
