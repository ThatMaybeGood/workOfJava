package com.mergedata.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class ApiRequestBodyList<T> {

    private List<T> list;

    // Getters and Setters

    // 静态方法，方便创建空的 body（例如查询失败时）
    public static <T> ApiRequestBodyList<T> empty() {
        ApiRequestBodyList<T> body = new ApiRequestBodyList<>();
        body.setList(java.util.Collections.emptyList());
        return body;
    }
}