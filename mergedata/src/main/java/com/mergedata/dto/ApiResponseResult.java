package com.mergedata.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true) // <--- 关键修改
public class ApiResponseResult {

    private String sign_type;
    private String sign;
    // 业务码，10000 表示成功
    private String code;
    private Object msg;
    private String sub_code;
    private String sub_msg;

    public boolean isSuccess() {
        // 根据您的 JSON 示例，业务成功码是 "10000"
        return "10000".equals(this.code);
    }

}
