package com.reports.dto.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 公共响应结果
 */
@Data
public class Result implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 签名类型
     */
    private String signType = "md5";

    /**
     * 响应码
     */
    private String code;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 业务响应码
     */
    private String subCode;

    /**
     * 业务响应消息
     */
    private String subMsg;

    /**
     * 是否成功
     */
    private Boolean success;

    public Result() {
    }

    public Result(String code, String msg, String subCode, String subMsg, Boolean success) {
        this.code = code;
        this.msg = msg;
        this.subCode = subCode;
        this.subMsg = subMsg;
        this.success = success;
    }

    /**
     * 成功响应
     */
    public static Result success() {
        return new Result("10000", "接口调用成功，并且业务系统也处理成功", "success", "处理成功", true);
    }

    /**
     * 成功响应（带业务消息）
     */
    public static Result success(String subMsg) {
        return new Result("10000", "接口调用成功，并且业务系统也处理成功", "success", subMsg, true);
    }

    /**
     * 失败响应
     */
    public static Result fail(String code, String msg, String subCode, String subMsg) {
        return new Result(code, msg, subCode, subMsg, false);
    }

}
