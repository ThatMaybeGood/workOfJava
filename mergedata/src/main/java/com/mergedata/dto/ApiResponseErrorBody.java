package com.mergedata.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseErrorBody<T> {
    private String timestamp;
    private Integer status;
    private String error;
    private String message;
    private String path;
    private String errorCode;
    private Object fieldErrors;
    private T data; // 如果有数据


    public ApiResponseErrorBody() {
        this.timestamp = java.time.LocalDateTime.now().toString();
    }

    public static <T> ApiResponseErrorBody<T> error(HttpStatus status, String error, String message, String path) {
        ApiResponseErrorBody<T> response = new ApiResponseErrorBody<>();
        response.setStatus(status.value());
        response.setError(error);
        response.setMessage(message);
        response.setPath(path);
        return response;
    }

    public static <T> ApiResponseErrorBody<T> error(HttpStatus status, String error, String message,
                                                String path, String errorCode) {
        ApiResponseErrorBody<T> response = error(status, error, message, path);
        response.setErrorCode(errorCode);
        return response;
    }

    public static <T> ApiResponseErrorBody<T> error(HttpStatus status, String error, String message,
                                                String path, String errorCode, Object fieldErrors) {
        ApiResponseErrorBody<T> response = error(status, error, message, path, errorCode);
        response.setFieldErrors(fieldErrors);
        return response;
    }
}