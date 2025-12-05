package com.mergedata.dto;

import lombok.Data;

@Data
public class ApiResponseErrorParams<T> {

    private ApiResponseResult result;
    // 构造参数缺少情况
    public static <T> ApiResponseErrorParams<T> failure() {
        ApiResponseErrorParams<T> response = new ApiResponseErrorParams<>();
        response.setResult(ApiResponseResult.errorParams());
         return response;
    }

}
