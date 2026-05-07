package com.messageTransformer.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

@Component("userStrategy")
public class UserStrategy implements MessageStrategy {

    @Override
    public Object transform(String body) {
        JSONObject json = JSON.parseObject(body);

        UserDTO dto = new UserDTO();
//        dto.setUserName(json.getString("name"));
//        dto.setAge(json.getInteger("age"));

        return dto;
    }
}