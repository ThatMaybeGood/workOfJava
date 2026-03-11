package com.example.auto_demo.Controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.auto_demo.logic.ApiLogic;
import com.example.auto_demo.model.ExportExcelDTO;
import com.example.auto_demo.util.Log;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    ApiLogic apiLogic;

    @RequestMapping(value = "/queryYbBill", method = RequestMethod.POST)
    public String queryYbBill(@RequestBody String input) {

        MDC.put("traceId", "YQ_" + System.currentTimeMillis());

        try {
            String sessionId = System.currentTimeMillis() + "|";

            Log.info("平台接口入参: {}", input);

            JSONObject jsonObject = JSONObject.parseObject(input);
            String billDate = jsonObject.getString("bill_date");
            String fixmedinsCode = jsonObject.getString("fixmedinsCode");
            String insuType = jsonObject.getString("insutype");
            String type = jsonObject.getString("type");

            JSONArray list = apiLogic.getBillList(sessionId, fixmedinsCode, billDate, insuType, type);

            JSONObject result = new JSONObject();
            result.put("rc", "1");
            result.put("msg", "成功");
            result.put("list", list);

            Log.info("平台接口出参: {}", result.toJSONString());

            return result.toJSONString();

        } catch (Exception e) {
            Log.error("平台接口执行异常", e);
            return "{\"rc\":\"0\",\"msg\":\"系统异常\"}";
        }
        finally {
            MDC.clear();
        }
    }
}