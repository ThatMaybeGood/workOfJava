package com.reports.exception;

import com.reports.enums.ResultCode;
import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String code;
    private final String subCode;
    private final String subMsg;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
        this.subCode = resultCode.getSubCode();
        this.subMsg = resultCode.getMsg();
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
        this.subCode = resultCode.getSubCode();
        this.subMsg = message;
    }

    public BusinessException(ResultCode resultCode, Throwable cause) {
        super(resultCode.getMsg(), cause);
        this.code = resultCode.getCode();
        this.subCode = resultCode.getSubCode();
        this.subMsg = resultCode.getMsg();
    }

}
