package com.mergedata.dto;

import lombok.Data;


/**
 * 通用请求报文，T为动态Body类型
 */
@Data
public class ApiRequest<T> {
    private ApiRequestHead head;
    private T body;
}
