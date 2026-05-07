package com.mergedata.model.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * SPD处方物流方式接收请求实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpdPrescExpressRequest {

    /**
     * 药房编码
     */
    @JsonProperty("StoreCode")
    private String StoreCode;

    /**
     * 处方日期 对应HIS待发处方表Presc_Date
     * 格式: yyyy-MM-dd HH:mm:ss
     */
    @JsonProperty("PrescDate")
    private String PrescDate;

    /**
     * 处方号 对应HIS待发处方表Presc_No
     */
    @JsonProperty("PrescNo")
    private String PrescNo;

    /**
     * 物流方式 1.快递，2.自取
     */
    @JsonProperty("IsExpress")
    private String IsExpress;
}
