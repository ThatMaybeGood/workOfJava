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

    /**
     * 子接口名，用于同一个 method 下再区分具体要哪块数据。
     * 例如住院预交金页面的 overview / summaryTable / incomeTable / refundTable /
     * trendChart / channelChart / payTypeChart 都走同一个 method，
     * 前端把 endpoint 一起传上来，后端据此返回对应的数据结构。
     */
    private String endpoint;

}
