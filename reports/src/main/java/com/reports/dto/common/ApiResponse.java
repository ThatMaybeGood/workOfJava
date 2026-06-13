package com.reports.dto.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应包装对象
 *
 * @param <T> 响应 Body 类型
 */
@Data
public class ApiResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应结果
     */
    private Result result;

    /**
     * 响应体
     */
    private T body;

    public ApiResponse() {
    }

    public ApiResponse(Result result, T body) {
        this.result = result;
        this.body = body;
    }

    /**
     * 成功响应
     */
    public static <T> ApiResponse<T> success(T body) {
        return new ApiResponse<>(Result.success(), body);
    }

    /**
     * 成功响应（带业务消息）
     */
    public static <T> ApiResponse<T> success(T body, String subMsg) {
        return new ApiResponse<>(Result.success(subMsg), body);
    }

    /**
     * 失败响应
     */
    public static <T> ApiResponse<T> fail(String code, String msg, String subCode, String subMsg) {
        return new ApiResponse<>(Result.fail(code, msg, subCode, subMsg), null);
    }

}
