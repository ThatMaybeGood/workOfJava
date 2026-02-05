package com.mergedata;

import com.mergedata.model.entity.YQOperatorEntity;
import com.mergedata.server.YQOperatorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class OperTests {
    @Autowired
    YQOperatorService yqOperatorService;
    @Test
    void oper(){
        Map<String, String> map = new HashMap<>();
        map.put("001","test");

        YQOperatorEntity yqOperator =new YQOperatorEntity();

        for (Map.Entry<String ,String> item:map.entrySet()){
            yqOperator.setOperatorNo(item.getKey());
            yqOperator.setOperatorName(item.getValue());
            yqOperator.setCategory("1");
            yqOperatorService.insert(yqOperator);
        }

    }
}
