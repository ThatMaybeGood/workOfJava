package com.mergedata.dto;

import lombok.Data;

@Data
public class ApiResponseErrorParams<T> {

    private ApiResponseResult result;
    // 构造参数缺少情况
    public static <T> ApiResponseErrorParams<T> failure( String errorMsg) {
        ApiResponseErrorParams<T> response = new ApiResponseErrorParams<>();
        response.setResult(ApiResponseResult.failureStatus(errorMsg));
         return response;
    }

    // 构造参数缺少情况
    public static <T> ApiResponseErrorParams<T> sucsc( String errorMsg) {
        ApiResponseErrorParams<T> response = new ApiResponseErrorParams<>();
        response.setResult(ApiResponseResult.failureStatus(errorMsg));
        return response;
    }
}
