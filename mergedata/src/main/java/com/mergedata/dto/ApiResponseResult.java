package com.mergedata.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true) // <--- 关键修改
public class ApiResponseResult {
    @JsonProperty("sign_type")
    private String signType;
    private String sign;
    // 业务码，10000 表示成功
    private String code;
    private Object msg;
    @JsonProperty("sub_code")
    private String subCode;
    @JsonProperty("sub_msg")
    private String subMsg;


     public  static ApiResponseResult successStatus() {
        ApiResponseResult result = new ApiResponseResult();
         result.setSignType("md5");
         result.setSign("s1tTc4F8ZYzdJ7HkNUJkZw=="); // 实际中这里应该动态生成
         result.setCode("10000");
         result.setMsg("接口调用成功，并且业务系统也处理成功");
         result.setSubCode("success");
         result.setSubMsg("查询报表信息成功！");
        return result;
    }

    public  static ApiResponseResult failureStatus() {
        ApiResponseResult result = new ApiResponseResult();
        result.setSignType("md5");
        result.setSign("s1tTc4F8ZYzdJ7HkNUJkZw==");
        result.setCode("40004");
        result.setMsg("业务处理失败");
        result.setSubCode("request_thirdparty_service_return_error");
        result.setSubMsg("查询报表信息失败！");
        return result;
    }

    public boolean isSuccess() {
        // 根据您的 JSON 示例，业务成功码是 "10000"
        return "10000".equals(this.code);
    }

}
