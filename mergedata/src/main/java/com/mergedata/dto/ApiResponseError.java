package com.mergedata.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseError<T> {
    private ApiResponseResult result;
    private ApiResponseErrorBody<T> body;



    public static <T> ApiResponseError<T> error(HttpStatus status, String error, String message, String path) {
        ApiResponseErrorBody<T> body = new ApiResponseErrorBody<>();

        ApiResponseError<T> response = new ApiResponseError<>();
        response.setResult(ApiResponseResult.errorParams("系统错误异常抛出"));
        response.setBody(body.error(status, error, message, path));
        return response;
    }

    public static <T> ApiResponseError<T> error(HttpStatus status, String error, String message,
                                                String path, String errorCode) {
        ApiResponseErrorBody<T> body = new ApiResponseErrorBody<>();

        ApiResponseError<T> response = new ApiResponseError<>();
        response.setResult(ApiResponseResult.errorParams("系统错误异常抛出"));


        response.setBody(body.error(status, error, message, path));

        return response;
    }

    public static <T> ApiResponseError<T> error(HttpStatus status, String error, String message,
                                                String path, String errorCode, Object fieldErrors) {
        ApiResponseErrorBody<T> body = new ApiResponseErrorBody<>();

        ApiResponseError<T> response = new ApiResponseError<>();
        response.setResult(ApiResponseResult.errorParams("系统错误异常抛出"));
        response.setBody(body.error(status, error, message, path, errorCode,fieldErrors));

        return response;
    }
}