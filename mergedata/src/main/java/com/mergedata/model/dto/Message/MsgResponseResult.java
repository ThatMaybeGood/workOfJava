package com.mergedata.model.dto.Message;

import lombok.Data;

@Data
public class MsgResponseResult {
    private String msg;
    private String code;


    // 构建成功响应 - 单对象
    public static MsgResponseResult success(String msg) {
        MsgResponseResult result = new MsgResponseResult();
        result.setMsg(msg);
        result.setCode("success");
        return result;
    }

    // 构建失败响应 - 单对象
    public static MsgResponseResult failure(String msg) {
        MsgResponseResult result = new MsgResponseResult();
        result.setMsg(msg);
        result.setCode("fail");
        return result;
    }
   }
