package com.example.auto_demo.logic;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.auto_demo.config.AppConfig;
import com.example.auto_demo.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ApiLogic {

    // 注入接口地址（直接注入字符串）
    @Value("${yb.bill.url}")
    private String billUrl;

    @Resource
    private AppConfig appConfig;

    private static AppConfig config;

    @PostConstruct
    public void init(){
        config = appConfig.getAppConfig() ;
    }

    public JSONArray getBillListByInsuType(String sessionId, String fixmedinsCode, String billDate,String insutype,String type) {

        JSONArray allbillList = new JSONArray();

        int pageNum = 1;

        log.info("读取参数配置文件:" + config.toString());
        int pageSize = Integer.valueOf(config.getPageSize());

        while (true){

            String  result = getBillDetail(sessionId,fixmedinsCode,billDate,insutype,pageNum,pageSize, type);
            JSONObject jsonObject = JSONObject.parseObject(result);
            JSONObject data = jsonObject.getJSONObject("data");
            JSONObject pageBean = data.getJSONObject("pageBean");
            JSONArray billList = pageBean.getJSONArray("data");
            allbillList.addAll(billList);
            pageNum ++;
            String lastPage = pageBean.getString("lastPage");
            if(lastPage.equals("true")){
                break;
            }
        }

        return allbillList;
    }

    public JSONArray getBillList(String sessionId, String fixmedinsCode, String billDate ,String insutype,String type) {

        if(StringUtils.isEmpty(insutype)){
            insutype = config.getInsuType();
        }

        if(StringUtils.isEmpty(fixmedinsCode)){
            fixmedinsCode = config.getFixmedinsCode();
        }

        if (StringUtils.isEmpty(fixmedinsCode)){
            fixmedinsCode = "H50010606446";
        }

        JSONArray allBillList = new JSONArray();

        String[] insuTypes = insutype.split(",");
        for (int i = 0; i < insuTypes.length; i++) {
            JSONArray billList = getBillListByInsuType(sessionId,fixmedinsCode,billDate,insuTypes[i], type);
            JSONArray newBillList =convertJSONArray(billList,insuTypes[i]);
            allBillList.addAll(newBillList);
        }
        return allBillList;
    }

    public JSONArray convertJSONArray(JSONArray orgList,String insutype) {
        JSONArray list = new JSONArray();
        for (int i = 0; i < orgList.size(); i++) {
            JSONObject jsonObject = orgList.getJSONObject(i);
            JSONObject newJsonObject = convertJSONObject(jsonObject,insutype);
            list.add(newJsonObject);
        }
        return list;
    }

    private JSONObject convertJSONObject(JSONObject jsonObject,String insutype) {
        JSONObject newJsonObject = new JSONObject();
        newJsonObject.put("setl_id",jsonObject.getString("setlId"));
        newJsonObject.put("mdtrt_id",jsonObject.getString("mdtrtId"));
        newJsonObject.put("psn_no",jsonObject.getString("psnNo"));
        newJsonObject.put("medfee_sumamt",jsonObject.getString("medfeeSumamt"));
        newJsonObject.put("bill_date",jsonObject.getString("setlTime"));
        newJsonObject.put("tran_type",jsonObject.getString("medfeeSumamt").contains("-")?"2":"1");
        newJsonObject.put("data_source","2");
        newJsonObject.put("psn_name",jsonObject.getString("psnName"));
        newJsonObject.put("insutype",insutype);
        newJsonObject.put("medins_setl_id",jsonObject.getString("medinsSetlId"));
        newJsonObject.put("msgid",jsonObject.getString("medinsSetlId"));

        return newJsonObject;
    }

    public String getBillDetail(String sessionId, String fixmedinsCode,String billDate,String insutype,
                                   int pageNum,int pageSize,String type) {
        Map<String, Object> map = new HashMap<>();

        map.put("fixmedinsCode", fixmedinsCode);
        map.put("billDate", billDate);
        map.put("pageNum", pageNum);
        map.put("setlTime",billDate);
        map.put("insutype",insutype);
        map.put("pageSize",pageSize);
        map.put("_modulePartId_","");
        String frontUrl = config.getFrontUrl();
        map.put("frontUrl",frontUrl);

        String url = config.getBillUrl();
        String token = config.getToken();
        String session = config.getSession();

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", "application/x-www-form-urlencoded");
        headerMap.put("X-XSRF-TOKEN", token);
        headerMap.put("Cookie", "XSRF-TOKEN=" + token + ";SESSION=" + session);

        log.info(sessionId + "调两定接口["+insutype+"]入参:" +  map.toString());

        String result = "";

        result = new HttpUtil().post(url, map, headerMap);


        log.info(sessionId + "调两定接口["+insutype+"]出参:" +  result);

        return result;
    }
}
