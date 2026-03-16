package com.mergedata.model.vo;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseError<T> {
    private ApiResponseResult result;
    private T body;

    public static <T> ApiResponseError<T> error(HttpStatus status, String error, String message, String path) {
        ApiResponseError<T> response = new ApiResponseError<>();
        ApiResponseResult result = ApiResponseResult.errorParams(message);

        response.setResult(result);
        response.setBody(null);
        return response;
    }

    public static <T> ApiResponseError<T> error(HttpStatus status, String error, String message,
                                                String path, String errorCode) {
        ApiResponseError<T> response = new ApiResponseError<>();
        ApiResponseResult result = ApiResponseResult.errorParams(message);
        result.setSubCode(errorCode);
        response.setResult(result);
        response.setBody(null);
        return response;
    }

    // 修改原有的 Object 参数方法
    public static <T> ApiResponseError<T> error(HttpStatus status, String error, String message,
                                                String path, String errorCode, Object fieldErrors) {
        ApiResponseError<T> response = new ApiResponseError<>();
        ApiResponseResult result = ApiResponseResult.errorParams(message);
        result.setSubCode(errorCode);

        response.setResult(result);
        response.setBody((T) fieldErrors);  // 强制转换
        return response;
    }

    // 新增专门处理 List<FieldErrorDetail> 的方法
    public static ApiResponseError<List<FieldErrorDetail>> error(
            HttpStatus status, String error, String message,
            String path, String errorCode, List<FieldErrorDetail> fieldErrors) {

        ApiResponseError<List<FieldErrorDetail>> response = new ApiResponseError<>();
        ApiResponseResult result = ApiResponseResult.errorParams(message);
        result.setSubCode(errorCode);

        response.setResult(result);
        response.setBody(fieldErrors);
        return response;
    }

    // 内部类
    @Data
    public static class FieldErrorDetail {
        private String field;
        private String message;

        public FieldErrorDetail(String field, String message) {
            this.field = field;
            this.message = message;
        }
    }

}