package com.reports.dto.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 请求报文 Head 部分
 */
@Data
public class RequestHead implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 字符集
     */
    private String charset = "utf-8";

    /**
     * 加密类型
     */
    private String encryptType = "AES";

    /**
     * 语言
     */
    private String language = "zh_CN";

    /**
     * 接口方法名，用于路由分发
     * 例如：reports.outp.outpatient-operation
     */
    private String method;

}
