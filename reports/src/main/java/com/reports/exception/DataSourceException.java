package com.reports.exception;

import com.reports.enums.ResultCode;
import lombok.Getter;

/**
 * 数据源异常
 */
@Getter
public class DataSourceException extends RuntimeException {

    private final String code;
    private final String subCode;

    public DataSourceException(String message) {
        super(message);
        this.code = ResultCode.DATASOURCE_ERROR.getCode();
        this.subCode = ResultCode.DATASOURCE_ERROR.getSubCode();
    }

    public DataSourceException(String message, Throwable cause) {
        super(message, cause);
        this.code = ResultCode.DATASOURCE_ERROR.getCode();
        this.subCode = ResultCode.DATASOURCE_ERROR.getSubCode();
    }

}
