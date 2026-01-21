package com.mergedata.exception;

import com.mergedata.model.vo.ApiResponseError;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理自定义业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponseError<?>> handleBusinessException(BusinessException ex,
                                                                       HttpServletRequest request) {
        logger.warn("业务异常: {}", ex.getMessage(), ex);

        ApiResponseError<?> errorResponse = ApiResponseError.error(
                HttpStatus.BAD_REQUEST,
                "业务逻辑错误",
                ex.getMessage(),  // ✅ 这里包含了具体的异常信息
                request.getRequestURI(),
                ex.getErrorCode()  // ✅ 包含错误码
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 专门处理请求体 JSON 格式错误或类型不匹配
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseError<?>> handleJsonFormatError(HttpMessageNotReadableException ex,
                                                                     HttpServletRequest request) {
        log.error("请求体格式或类型转换错误", ex);

        String detailedMessage = "请求体JSON格式错误或字段类型不匹配。";
        if (ex.getRootCause() != null) {
            detailedMessage += " 详情: " + ex.getRootCause().getMessage();
        }

        ApiResponseError<?> errorResponse = ApiResponseError.error(
                HttpStatus.BAD_REQUEST,
                "请求格式错误",
                detailedMessage,  // ✅ 包含详细的错误信息
                request.getRequestURI(),
                "4000"  // JSON格式错误码
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseError<?>> handleValidationException(MethodArgumentNotValidException ex,
                                                                         HttpServletRequest request) {
        logger.warn("参数验证失败: {}", ex.getMessage());

        // 提取详细的字段错误信息
        List<FieldErrorDetail> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorDetail(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());

        ApiResponseError<?> errorResponse = ApiResponseError.error(
                HttpStatus.BAD_REQUEST,
                "参数验证失败",
                "请求参数不合法",
                request.getRequestURI(),
                "4001",
                fieldErrors  // ✅ 包含字段级错误详情
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponseError<?>> handleConstraintViolationException(ConstraintViolationException ex,
                                                                                  HttpServletRequest request) {
        logger.warn("约束违反异常: {}", ex.getMessage());

        List<FieldErrorDetail> fieldErrors = ex.getConstraintViolations().stream()
                .map(violation -> {
                    String fieldName = violation.getPropertyPath().toString();
                    return new FieldErrorDetail(fieldName, violation.getMessage());
                })
                .collect(Collectors.toList());

        ApiResponseError<?> errorResponse = ApiResponseError.error(
                HttpStatus.BAD_REQUEST,
                "参数验证失败",
                "请求参数不合法",
                request.getRequestURI(),
                "4002",
                fieldErrors  // ✅ 包含字段级错误详情
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 处理数据库连接异常
     */
    @ExceptionHandler(DatabaseConnectionException.class)
    public ResponseEntity<ApiResponseError<?>> handleDatabaseConnectionException(DatabaseConnectionException ex,
                                                                                 HttpServletRequest request) {
        logger.error("数据库连接异常: {}", ex.getMessage(), ex);

        ApiResponseError<?> errorResponse = ApiResponseError.error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "数据库连接错误",
                "系统正在维护或数据库连接失败：" + ex.getMessage(),  // ✅ 包含具体的数据库错误信息
                request.getRequestURI(),
                "5001"  // 数据库连接错误码
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 处理数据库访问异常
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponseError<?>> handleDataAccessException(DataAccessException ex,
                                                                         HttpServletRequest request) {
        logger.error("数据库访问异常: {}", ex.getMessage(), ex);

        String detailedMessage = "数据库操作失败，请检查数据权限或 SQL 语句。";
        // 可以添加更具体的数据库错误信息
        if (ex.getCause() != null) {
            detailedMessage += " 原因: " + ex.getCause().getMessage();
        }

        ApiResponseError<?> errorResponse = ApiResponseError.error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "数据库操作失败",
                detailedMessage,  // ✅ 包含数据库操作的具体错误
                request.getRequestURI(),
                "5002"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponseError<?>> handleNullPointerException(NullPointerException ex,
                                                                          HttpServletRequest request) {
        logger.error("空指针异常", ex);

        // 生产环境可以隐藏详细堆栈，开发环境可以显示
        String message = "系统出现未预期的错误";
        if (isDevelopment()) {
            message += ": " + ex.getMessage();
            if (ex.getStackTrace().length > 0) {
                message += " at " + ex.getStackTrace()[0];
            }
        }

        ApiResponseError<?> errorResponse = ApiResponseError.error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "系统内部错误",
                message,  // ✅ 根据环境显示不同的错误信息
                request.getRequestURI(),
                "5000"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 处理404资源未找到异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponseError<?>> handleNoHandlerFoundException(NoHandlerFoundException ex,
                                                                             HttpServletRequest request) {
        logger.warn("资源未找到: {}", ex.getMessage());

        ApiResponseError<?> errorResponse = ApiResponseError.error(
                HttpStatus.NOT_FOUND,
                "资源未找到",
                "请求的资源不存在: " + request.getRequestURI(),  // ✅ 包含请求路径
                request.getRequestURI(),
                "404"
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * 处理所有其他未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseError<?>> handleGeneralException(Exception ex,
                                                                      HttpServletRequest request) {
        logger.error("未处理的异常: {}", ex.getMessage(), ex);

        String message = "服务器内部错误，请联系管理员";
        // 开发环境显示更详细信息
        if (isDevelopment() && ex.getMessage() != null) {
            message += "。错误详情: " + ex.getMessage();
        }

        ApiResponseError<?> errorResponse = ApiResponseError.error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "系统错误",
                message,  // ✅ 包含异常信息
                request.getRequestURI(),
                "500"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 内部类：字段错误详情
     */
    @Data
    public static class FieldErrorDetail {
        private String field;
        private String message;

        public FieldErrorDetail(String field, String message) {
            this.field = field;
            this.message = message;
        }
    }

    /**
     * 判断是否为开发环境
     */
    private boolean isDevelopment() {
        // 这里根据你的配置判断环境
        String env = System.getProperty("spring.profiles.active", "dev");
        return "dev".equals(env) || "development".equals(env);
    }
}