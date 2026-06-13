package com.reports.enums;

import lombok.Getter;

/**
 * 统一结果码枚举
 */
@Getter
public enum ResultCode {

    /**
     * 成功
     */
    SUCCESS("10000", "接口调用成功，并且业务系统也处理成功", "success"),

    /**
     * 参数错误
     */
    PARAM_ERROR("20001", "请求参数错误", "param_error"),

    /**
     * 参数缺失
     */
    PARAM_MISSING("20002", "必填参数缺失", "param_missing"),

    /**
     * 参数格式错误
     */
    PARAM_FORMAT_ERROR("20003", "参数格式错误", "param_format_error"),

    /**
     * 方法不存在
     */
    METHOD_NOT_FOUND("30001", "请求的方法不存在", "method_not_found"),

    /**
     * 方法未实现
     */
    METHOD_NOT_IMPLEMENTED("30002", "请求的方法尚未实现", "method_not_implemented"),

    /**
     * 数据不存在
     */
    DATA_NOT_FOUND("40001", "查询的数据不存在", "data_not_found"),

    /**
     * 数据库异常
     */
    DB_ERROR("50001", "数据库操作异常", "db_error"),

    /**
     * 数据源异常
     */
    DATASOURCE_ERROR("50002", "数据源切换异常", "datasource_error"),

    /**
     * SQL执行异常
     */
    SQL_EXEC_ERROR("50003", "SQL执行异常", "sql_exec_error"),

    /**
     * 系统内部错误
     */
    SYSTEM_ERROR("99999", "系统内部错误", "system_error");

    private final String code;
    private final String msg;
    private final String subCode;

    ResultCode(String code, String msg, String subCode) {
        this.code = code;
        this.msg = msg;
        this.subCode = subCode;
    }

}
