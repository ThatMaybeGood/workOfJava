package com.mergedata.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class ApiResponseBodyList<T> {

    private List<T> list;


    // Getters and Setters

    // 静态方法，方便创建空的 body（例如查询失败时）
    public static <T> ApiResponseBodyList<T> empty() {
        ApiResponseBodyList<T> body = new ApiResponseBodyList<>();
        body.setList(java.util.Collections.emptyList());
        return body;
    }


}