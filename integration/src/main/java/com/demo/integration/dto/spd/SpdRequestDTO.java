package com.demo.integration.dto.spd;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2026/5/9 23:06
 */

import lombok.Data;

@Data
public class SpdRequestDTO {

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 患者姓名
     */
    private String patientName;
}