package com.mergedata.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;


/**
 * 通用请求报文，T为动态Body类型
 */
@Data
// 核心：确保顶层字段 (head, body) 在 JSON 中是小写下划线
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ApiRequest<T> {
    private ApiRequestHead head;
    private T body;
}
