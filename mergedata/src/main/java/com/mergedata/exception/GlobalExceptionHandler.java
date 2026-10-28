package com.mergedata.exception;

import com.mergedata.model.vo.ApiResponseError;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final Pattern ORA_CODE_PATTERN = Pattern.compile("ORA-\\d{5}");

    private final Environment environment;

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
                ex.getMessage(),
                request.getRequestURI(),
                ex.getErrorCode()
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

        String detailedMessage = "请求体JSON格式错误或字段类型不匹配，请检查提交内容";
        if (isDevelopment() && ex.getRootCause() != null) {
            detailedMessage += "。详情: " + ex.getRootCause().getMessage();
        }

        ApiResponseError<?> errorResponse = ApiResponseError.error(
                HttpStatus.BAD_REQUEST,
                "请求格式错误",
                detailedMessage,
                request.getRequestURI(),
                "4000"
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
                fieldErrors
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
                fieldErrors
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
                "系统正在维护或数据库连接失败，请稍后重试",
                request.getRequestURI(),
                "5001"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 处理数据库访问异常——按错误码分类返回脱敏提示，详情仅记日志
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponseError<?>> handleDataAccessException(DataAccessException ex,
                                                                         HttpServletRequest request) {
        logger.error("数据库访问异常: {}", ex.getMessage(), ex);

        String detailedMessage = friendlyDbMessage(ex);
        if (isDevelopment()) {
            detailedMessage += "。详情: " + rootCauseMessage(ex);
        }

        ApiResponseError<?> errorResponse = ApiResponseError.error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "数据库操作失败",
                detailedMessage,
                request.getRequestURI(),
                "5002"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 缺少必填请求参数
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponseError<?>> handleMissingParameter(MissingServletRequestParameterException ex,
                                                                      HttpServletRequest request) {
        logger.warn("缺少必填参数: {}", ex.getParameterName());

        ApiResponseError<?> errorResponse = ApiResponseError.error(
                HttpStatus.BAD_REQUEST,
                "参数缺失",
                "缺少必填参数: " + ex.getParameterName(),
                request.getRequestURI(),
                "4003"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 路径变量缺失
     */
    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ApiResponseError<?>> handleMissingPathVariable(MissingPathVariableException ex,
                                                                         HttpServletRequest request) {
        logger.warn("缺少路径变量: {}", ex.getVariableName());

        ApiResponseError<?> errorResponse = ApiResponseError.error(
                HttpStatus.BAD_REQUEST,
                "参数缺失",
                "请求路径不完整，缺少: " + ex.getVariableName(),
                request.getRequestURI(),
                "4004"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 参数类型不匹配
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponseError<?>> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                                  HttpServletRequest request) {
        logger.warn("参数类型不匹配: {}={}", ex.getName(), ex.getValue());

        ApiResponseError<?> errorResponse = ApiResponseError.error(
                HttpStatus.BAD_REQUEST,
                "参数格式错误",
                "参数 " + ex.getName() + " 格式不正确",
                request.getRequestURI(),
                "4005"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 表单绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponseError<?>> handleBindException(BindException ex,
                                                                   HttpServletRequest request) {
        logger.warn("表单绑定失败: {}", ex.getMessage());

        List<FieldErrorDetail> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorDetail(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());

        ApiResponseError<?> errorResponse = ApiResponseError.error(
                HttpStatus.BAD_REQUEST,
                "参数验证失败",
                "请求参数不合法",
                request.getRequestURI(),
                "4006",
                fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 请求方式不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponseError<?>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                        HttpServletRequest request) {
        logger.warn("请求方式不支持: {} {}", ex.getMethod(), request.getRequestURI());

        ApiResponseError<?> errorResponse = ApiResponseError.error(
                HttpStatus.METHOD_NOT_ALLOWED,
                "请求方式错误",
                "该接口不支持 " + ex.getMethod() + " 请求方式",
                request.getRequestURI(),
                "4007"
        );
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }

    /**
     * 请求内容类型不支持
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponseError<?>> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
                                                                           HttpServletRequest request) {
        logger.warn("Content-Type不支持: {}", ex.getContentType());

        ApiResponseError<?> errorResponse = ApiResponseError.error(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "请求格式错误",
                "请求内容类型不支持，请使用 application/json",
                request.getRequestURI(),
                "4008"
        );
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(errorResponse);
    }

    /**
     * 上传文件大小超限
     */
    @ExceptionHandler({MaxUploadSizeExceededException.class, MultipartException.class})
    public ResponseEntity<ApiResponseError<?>> handleUploadSizeExceeded(MultipartException ex,
                                                                        HttpServletRequest request) {
        logger.warn("上传文件超限: {}", ex.getMessage());

        ApiResponseError<?> errorResponse = ApiResponseError.error(
                HttpStatus.BAD_REQUEST,
                "上传失败",
                "上传文件大小超出限制，请压缩后重试",
                request.getRequestURI(),
                "4009"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 异步请求超时
     */
    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public ResponseEntity<ApiResponseError<?>> handleAsyncTimeout(AsyncRequestTimeoutException ex,
                                                                  HttpServletRequest request) {
        logger.warn("异步请求超时: {}", request.getRequestURI());

        ApiResponseError<?> errorResponse = ApiResponseError.error(
                HttpStatus.SERVICE_UNAVAILABLE,
                "请求超时",
                "请求处理超时，请稍后重试",
                request.getRequestURI(),
                "4010"
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponseError<?>> handleNullPointerException(NullPointerException ex,
                                                                          HttpServletRequest request) {
        logger.error("空指针异常", ex);

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
                message,
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
                "请求的资源不存在: " + request.getRequestURI(),
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
        if (isDevelopment() && ex.getMessage() != null) {
            message += "。错误详情: " + ex.getMessage();
        }

        ApiResponseError<?> errorResponse = ApiResponseError.error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "系统错误",
                message,
                request.getRequestURI(),
                "500"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 按Oracle错误码映射脱敏提示，避免暴露表名/字段名/SQL
     */
    private String friendlyDbMessage(DataAccessException ex) {
        String oraCode = extractOraCode(ex);
        if (oraCode != null) {
            switch (oraCode) {
                case "ORA-00001":
                    return "数据已存在，请勿重复提交";
                case "ORA-01400":
                    return "存在必填字段未填写，请检查输入内容";
                case "ORA-01722":
                    return "数据格式不正确，请检查输入内容";
                case "ORA-12899":
                    return "输入内容长度超出限制，请缩短后重试";
                case "ORA-02291":
                    return "关联的数据不存在，请检查后重试";
                case "ORA-02292":
                    return "该数据已被其他记录引用，无法删除";
                case "ORA-00942":
                    return "数据库访问异常，请联系管理员";
                default:
                    break;
            }
        }
        String rootMsg = rootCauseMessage(ex);
        if (rootMsg != null && (rootMsg.contains("CommunicationsException")
                || rootMsg.contains("SocketTimeout")
                || rootMsg.contains("ORA-17002")
                || rootMsg.contains("ORA-12170"))) {
            return "数据库连接异常，请稍后重试";
        }
        return "数据库操作失败，请稍后重试或联系管理员";
    }

    /**
     * 沿异常链找ORA错误码
     */
    private String extractOraCode(Throwable ex) {
        Throwable t = ex;
        while (t != null) {
            String msg = t.getMessage();
            if (msg != null) {
                Matcher m = ORA_CODE_PATTERN.matcher(msg);
                if (m.find()) {
                    return m.group();
                }
            }
            t = t.getCause();
        }
        return null;
    }

    /**
     * 取最底层异常消息
     */
    private String rootCauseMessage(Throwable ex) {
        Throwable t = ex;
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t.getMessage();
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
     * 判断是否为开发环境——读Spring真实profile，避免系统属性缺省误判
     */
    private boolean isDevelopment() {
        for (String profile : environment.getActiveProfiles()) {
            if ("dev".equals(profile) || "development".equals(profile) || "mine".equals(profile)) {
                return true;
            }
        }
        return false;
    }
}
