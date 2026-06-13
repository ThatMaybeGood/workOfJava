package com.reports.dto.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一请求包装对象
 *
 * @param <T> 请求 Body 类型
 */
@Data
public class ApiRequest<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 请求头
     */
    private RequestHead head;

    /**
     * 请求体
     */
    private T body;

    /**
     * 获取 method
     */
    public String getMethod() {
        return head != null ? head.getMethod() : null;
    }

}
