package com.mergedata.model.vo;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class ApiResponse<T> {
    private ApiResponseResult result;
    private T body;


    // 构建成功响应 - 列表
    public static <T> ApiResponse<ApiResponseBodyList<T>> successList(List<T> dataList,String msg) {
        ApiResponse<ApiResponseBodyList<T>> response = new ApiResponse<>();


        // 假设 ApiResponseResult 也有静态方法构造成功状态
        response.setResult(ApiResponseResult.successStatus(msg));

        ApiResponseBodyList<T> body = new ApiResponseBodyList<>();
        body.setList(dataList);
        response.setBody(body);
        return response;
    }

    // 构造成功的单对象
    public static <T> ApiResponse<T> successObj(T obj,String msg) {
        ApiResponse<T> response = new ApiResponse<>();

        // 假设 ApiResponseResult 也有静态方法构造成功状态
        response.setResult(ApiResponseResult.successStatus(msg));
        response.setBody(obj);
        return response;
    }


    // 构造失败的响应 (如您示例所示)
    public static <T> ApiResponse<T> failure(String erorMsg) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setResult(ApiResponseResult.failureStatus(erorMsg));
//        response.setBody(ApiResponseBodyList.empty()); // 失败时 body.list 为空
        return response;
    }

    // 构造失败的响应 (如您示例所示)
    public static <T> ApiResponse<T> success(String erorMsg) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setResult(ApiResponseResult.successStatus(erorMsg));
//        response.setBody(ApiResponseBodyList.empty()); // 失败时 body.list 为空
        return response;
    }
}
