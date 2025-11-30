package com.mergedata.dto;

import lombok.Data;

import java.util.List;

@Data
public class ApiResponse<T> {
    private ApiResponseResult result;
    private ApiResponseBody<T> body;


    // --- 静态构造方法示例 ---

    // 构造成功的响应
    public static <T> ApiResponse<T> success(List<T> dataList) {
        ApiResponse<T> response = new ApiResponse<>();


        // 假设 ApiResponseResult 也有静态方法构造成功状态
        response.setResult(ApiResponseResult.successStatus());

        ApiResponseBody<T> body = new ApiResponseBody<>();
        body.setList(dataList);
        response.setBody(body);
        return response;
    }

    // 构造失败的响应 (如您示例所示)
    public static <T> ApiResponse<T> failure(String errorCode, String errorMsg) {
        ApiResponse<T> response = new ApiResponse<>();

//        // 假设 ApiResponseResult 内部逻辑可以根据输入构建失败状态
//        ApiResponseResult status = new ApiResponseResult();
//        status.setCode(errorCode);
//        status.setMsg(errorMsg);
//        status.setSubCode("failure");
//        // ... 设置其他失败字段 ...

        response.setResult(ApiResponseResult.failureStatus());
        response.setBody(ApiResponseBody.empty()); // 失败时 body.list 为空
        return response;
    }
}
