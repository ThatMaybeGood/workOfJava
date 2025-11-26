package com.mergedata.entity;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CommonRequestBody {
    private String reportDate;
    private String extendParam1;
    private String extendParam2;
    private String extendParam3;
}
