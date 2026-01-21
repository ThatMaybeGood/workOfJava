package com.mergedata.model.dto;

import lombok.Data;


/**
 * 通用请求报文，T为动态Body类型
 */
@Data
public class ApiRequestList<T> {
    private ApiRequestHead head;
    private ApiRequestBodyList<T> body;

}
