package com.messageTransformer.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.messageTransformer.dto.OrderDTO;
import org.springframework.stereotype.Component;

@Component("orderStrategy")
public class OrderStrategy implements MessageStrategy {

    @Override
    public Object transform(String body) {
        // 解析原始消息（JSON/XML都行）
        JSONObject json = JSON.parseObject(body);

        OrderDTO dto = new OrderDTO();
        dto.setOrderId(json.getString("id"));
        dto.setAmount(json.getBigDecimal("amount"));

        return dto;
    }
}