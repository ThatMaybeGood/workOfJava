package com.mergedata.dto;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private ApiResponseResult result;
    private ApiResponseBody<T> body;

}
