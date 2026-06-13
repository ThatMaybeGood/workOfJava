package com.reports.dto.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 请求报文 Body 基类
 */
@Data
public class BaseRequestBody implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 扩展参数1
     */
    private Object extendParams1;

    /**
     * 扩展参数2
     */
    private Object extendParams2;

    /**
     * 扩展参数3
     */
    private Object extendParams3;

}
