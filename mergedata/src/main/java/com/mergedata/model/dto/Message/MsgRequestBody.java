package com.mergedata.model.dto.Message;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class MsgRequestBody {
    @JsonAlias({"app_id", "appId"})
    private String appId;

    @JsonAlias({"resource_code", "resourceCode"})
    private String resourceCode;

    @JsonAlias({"enterprise_id", "enterpriseId"})
    private String enterpriseId;

    @JsonAlias({"msg_code", "msgCode"})
    private String msgCode;

    @JsonAlias({"user_id", "userId"})
    private String userId;

    @JsonAlias({"identity_id", "identityId"})
    private String identityId;

    @JsonAlias({"identity_type", "identityType"})
    private String identityType;

    private String mobile;
    private String email;

    @JsonAlias({"text_msg", "textMsg"})
    private String textMsg;

    private String data;  //接收为String  需要单独转为对象

    // 额外字段：从请求中看到的其他字段
    private String channel_code;
    private String client_msg_id;
    private Object info;  // info对象
    private String template_config;
    private String unique_id;
    private String msg_name;
    private String patient_name;
    private String subscriber_info;

    /**
     * 获取解析后的 MsgRequestData 对象
     */
    public MsgRequestData getDataObject() {
        if (data == null || data.isEmpty()) {
            log.warn("data 字段为空");
            return null;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            MsgRequestData msgRequestData = mapper.readValue(data, MsgRequestData.class);
//            log.info("成功解析 data 字符串为对象: {}", msgRequestData);
            return msgRequestData;
        } catch (JsonProcessingException e) {
            log.error("解析 data 字符串失败: {}", data, e);
            return null;
        }
    }
}