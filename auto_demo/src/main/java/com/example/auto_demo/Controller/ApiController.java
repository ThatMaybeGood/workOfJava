package com.example.auto_demo.Controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.auto_demo.logic.ApiLogic;
import com.example.auto_demo.model.ExportExcelDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    ApiLogic apiLogic;

    @RequestMapping(value = "/queryYbBill",method = RequestMethod.POST)
    public String queryYbBill(@RequestBody String input) {

        String sessionId =  String.valueOf(System.currentTimeMillis()) + "|";

        log.info(sessionId + "平台接口入参:" +  input);

        JSONObject jsonObject = JSONObject.parseObject(input);

        String billDate = jsonObject.getString("bill_date");
        String fixmedinsCode = jsonObject.getString("fixmedinsCode");
        String insuType = jsonObject.getString("insutype");
        String type = jsonObject.getString("type");

        JSONArray list =  apiLogic.getBillList(sessionId,fixmedinsCode,billDate,insuType,type);

        //----------------------------增加调用文件导出-----------------------------------------
//        JSONArray list  = apiLogic.getBillDetailExport(sessionId, fixmedinsCode, billDate, insuType, type);

        JSONObject result = new JSONObject();
        result.put("rc","1");
        result.put("msg","成功");
        result.put("list",list);

        log.info(sessionId + "平台接口出参:" +  result.toJSONString());

        return result.toJSONString();
    }

}
