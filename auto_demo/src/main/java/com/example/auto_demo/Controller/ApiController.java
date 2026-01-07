package com.example.auto_demo.Controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.auto_demo.logic.ApiLogic;
import com.example.auto_demo.logic.WebDriverLogic;
import com.example.auto_demo.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
public class ApiController {

    @RequestMapping(value = "/getYbBill",method = RequestMethod.POST)
    public String getBillData(@RequestBody String input) {

        String sessionId = StringUtil.getSessionId();
        log.info(sessionId + "开始执行");
        JSONObject jsonObject = JSONObject.parseObject(input);

        String type = jsonObject.getString("type");
        //type  1 按天  2 按月
        if(StringUtils.isEmpty(type)){
            type = "1";
        }

        String date = jsonObject.getString("date");

        try {
            new WebDriverLogic().uploadFile(sessionId,type,date);
        }catch (Exception e){
            log.error(sessionId + "执行异常",e);
            return "执行异常";
        }

        log.info(sessionId + "结束执行");

        return "成功";
    }

    @RequestMapping(value = "/queryYbBill",method = RequestMethod.POST)
    public String queryYbBill(@RequestBody String input) {

        String sessionId = StringUtil.getSessionId();

        log.info(sessionId + "平台接口入参:" +  input);

        JSONObject jsonObject = JSONObject.parseObject(input);

        String billDate = jsonObject.getString("bill_date");
        String fixmedinsCode = jsonObject.getString("fixmedinsCode");
        String insuType = jsonObject.getString("insutype");

        String type = jsonObject.getString("type");

        JSONArray list = new ApiLogic().getBillList(sessionId,fixmedinsCode,billDate,insuType,type);

        JSONObject result = new JSONObject();
        result.put("rc","1");
        result.put("msg","成功");
        result.put("list",list);

        log.info(sessionId + "平台接口出参:" +  result.toJSONString());

        return result.toJSONString();
    }

}
