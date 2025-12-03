package com.mergedata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mergedata.dto.ApiRequestBodyList;
import com.mergedata.dto.ApiRequestHead;
import com.mergedata.dto.ApiRequestList;
import com.mergedata.dto.CommonRequestBody;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class MessageDataApplicationTests {
    @Autowired
    ApiRequestHead apiRequestHead;
    @Test
    void contextLoads()  {
        List<CommonRequestBody> list  = new ArrayList<>();


        CommonRequestBody common  = new CommonRequestBody();
        common.setReportdate("2023-01-01");
        list.add(common);

        ApiRequestBodyList<CommonRequestBody> body  = new ApiRequestBodyList<>();
        body.setList(list);


        ApiRequestList<CommonRequestBody> request   = new ApiRequestList<>();
        request.setHead(apiRequestHead);
        request.setBody(body);

        try {

            // 1. 创建 ObjectMapper 实例 (线程安全，可以重用)
            ObjectMapper objectMapper = new ObjectMapper();
// 3. 将 Java 对象序列化为 JSON 字符串
            String jsonString = objectMapper.writeValueAsString(request);

            // 打印 JSON 字符串
            System.out.println("JSON 格式 (一行): " + jsonString);
        }catch (Exception e)
            {
                e.printStackTrace();
            }


//        System.out.printf(request.toString());


    }

}
