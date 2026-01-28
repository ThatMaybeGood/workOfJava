package com.mergedata.model.vo;

import lombok.Data;


@Data
public class ApiResponseBodyObj<T> {
    private T obj;
}
