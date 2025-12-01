package com.mergedata.exception;

/**
 * 自定义数据库连接异常，用于包装底层的连接失败、超时等 DataAccessException。
 * 这样可以在业务代码中清晰地区分不同类型的数据库错误。
 */
public class DatabaseConnectionException extends RuntimeException {

    // 推荐的构造函数 1: 仅包含消息
    public DatabaseConnectionException(String message) {
        super(message);
    }

    // 推荐的构造函数 2: 包含消息和导致该异常的原始Throwable（如底层的SQLException或TimeoutException）
    public DatabaseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}