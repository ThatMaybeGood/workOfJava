package com.mergedata.model.dto;

import lombok.Data;

import javax.validation.Valid;

/**
 * 通用请求报文，T为动态Body类型
 */
@Data
public class ApiRequest<T> {

    @Valid
    private ApiRequestHead head;

    @Valid
    private T body;

}
