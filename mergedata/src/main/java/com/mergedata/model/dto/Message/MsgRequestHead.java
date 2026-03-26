package com.mergedata.model.dto.Message;

import lombok.Data;


@Data
public class MsgRequestHead {
    private String method;
    private String version;
    private String charset;
    private String language;
    private String sysTrackCode;
    private String enterpriseId;
    private String timestamp;
    private String encryptType;
    private String appId;
    private String resourceCode;
}