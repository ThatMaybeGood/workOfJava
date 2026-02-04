package com.mergedata.model.dto;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 请求头
 */
@Data
public class ApiRequestHead {

    private String charset;
    private String encryptType;
    private String enterpriseId;
    private String language;
    private String method;
    private String sign;
    private String signType;
    private String sysTrackCode;
    private String timestamp;
    private String version;
    private String accessToken;
    private String appId;
}
