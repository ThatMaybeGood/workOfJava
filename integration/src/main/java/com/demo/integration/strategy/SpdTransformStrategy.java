package com.demo.integration.strategy;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2026/5/9 23:31
 */

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.demo.integration.dto.spd.SpdRequestDTO;
import org.springframework.stereotype.Component;

@Component
public class SpdTransformStrategy
        implements MessageTransformStrategy {

    @Override
    public Object transform(String body) {

        JSONObject jsonObject =
                JSON.parseObject(body);

        SpdRequestDTO dto = new SpdRequestDTO();

        dto.setOrderNo(
                jsonObject.getString("orderNo")
        );

        dto.setPatientName(
                jsonObject.getString("patientName")
        );

        return dto;
    }
}