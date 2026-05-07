package com.mergedata.model.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * SPD处方物流方式接收响应实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpdPrescExpressResponse {

    /**
     * 调用结果 1001:成功 其他:失败
     */
    @JsonProperty("ResultInfo")
    private String ResultInfo;

    /**
     * 返回描述，例如：成功
     * 若调用失败，此处返回错误详细信息
     */
    @JsonProperty("ResultMessage")
    private String ResultMessage;

    /**
     * 内容（可为null）
     */
    @JsonProperty("Content")
    private String Content;

    /**
     * 判断是否调用成功
     */
    public boolean isSuccess() {
        return "1001".equals(this.ResultInfo);
    }
}