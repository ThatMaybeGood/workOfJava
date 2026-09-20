package com.reports.dto.response.outpatient.internet.hospital;

import lombok.Data;

/**
 * 互医质控运营月报维护项
 */
@Data
public class IhMaintainItem {

    /**
     * 指标编码（对应 tr_inet_hosp_ov 列名）
     */
    private String indicatorCode;

    /**
     * 指标名称
     */
    private String indicatorName;

    /**
     * 指标值（门诊量为纯数字，比率支持 "73.5" 或 "73.5%"）
     */
    private String value;
}
