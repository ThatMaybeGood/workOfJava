package com.mergedata.model.dto.Message;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MsgRequestData {
    @JsonAlias({"patientName", "patient_name"})
    private String patientName;

    @JsonAlias({"sex"})
    private String sex;

    @JsonAlias({"patientId", "patient_id"})
    private String patientId;

    @JsonAlias({"birthday"})
    private String birthday;

    @JsonAlias({"telephone"})
    private String telephone;

    @JsonAlias({"idType", "id_type"})
    private String idType;

    @JsonAlias({"idCard", "id_card"})
    private String idCard;

    @JsonAlias({"prioCategoryName", "prio_category_name"})
    private String prioCategoryName;

    @JsonAlias({"prioCategoryNum", "prioCategoryNum"})
    private String prioCategoryNum;

    @JsonAlias({"effectiveType", "effective_type"})
    private String effectiveType;

    @JsonAlias({"effectiveBeginTime", "effective_begin_time"})
    private String effectiveBeginTime;

    @JsonAlias({"effectiveEndTime", "effective_end_time"})
    private String effectiveEndTime;

    private String address;

    @JsonAlias({"detailAddress", "detail_address"})
    private String detailAddress;

    @JsonAlias({"imgUrl", "img_url"})
    private String imgUrl;

    private String remarks;

    @JsonAlias({"isEnable", "is_enable"})
    private String isEnable;  // 是否启用 1：是 0：否 默认1  修改新增操作都是1 ，删除就是0

    @JsonAlias({"validState", "valid_state"})
    private String validState;
}