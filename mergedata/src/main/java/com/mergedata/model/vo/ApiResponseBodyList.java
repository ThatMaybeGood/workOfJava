package com.mergedata.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class ApiResponseBody<T> {

    private List<T> list;

    private T  obj;

    // Getters and Setters

    // 静态方法，方便创建空的 body（例如查询失败时）
    public static <T> ApiResponseBody<T> empty() {
        ApiResponseBody<T> body = new ApiResponseBody<>();
        body.setList(java.util.Collections.emptyList());
        return body;
    }


}