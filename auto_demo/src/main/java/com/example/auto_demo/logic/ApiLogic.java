package com.example.auto_demo.logic;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.example.auto_demo.config.AppConfig;
import com.example.auto_demo.model.ExportExcelDTO;
import com.example.auto_demo.util.ExportUtil;
import com.example.auto_demo.util.HttpUtil;
import com.example.auto_demo.util.InsuType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ApiLogic {

    @Autowired
    private AppConfig config;

    @Autowired
    private ObjectMapper objectMapper; // Spring Boot 自动配置


    public JSONArray getBillListByInsuType(String sessionId, String fixmedinsCode, String billDate, String insutype, String type) {

        JSONArray allbillList = new JSONArray();

        int pageNum = 1;

        log.info("读取参数配置文件:" + config.toString());
        int pageSize = Integer.valueOf(config.getPageSize());

        while (true) {
            String result = null;

            result = getBillDetail(sessionId, fixmedinsCode, billDate, insutype, pageNum, pageSize, type);
            JSONObject jsonObject = JSONObject.parseObject(result);
            JSONObject data = jsonObject.getJSONObject("data");
            JSONObject pageBean = data.getJSONObject("pageBean");
            JSONArray billList = pageBean.getJSONArray("data");
            allbillList.addAll(billList);
            pageNum++;
            String lastPage = pageBean.getString("lastPage");
            if (lastPage.equals("true")) {
                break;
            }

        }

        return allbillList;
    }

    public JSONArray getBillList(String sessionId, String fixmedinsCode, String billDate, String insutype, String type) {

        if (StringUtils.isEmpty(insutype)) {
            insutype = config.getInsuType();
        }

        if (StringUtils.isEmpty(fixmedinsCode)) {
            fixmedinsCode = config.getFixmedinsCode();
        }

        if (StringUtils.isEmpty(fixmedinsCode)) {
            fixmedinsCode = "H50010606446";
        }

        JSONArray allBillList = new JSONArray();

        String[] insuTypes = insutype.split(",");
        for (int i = 0; i < insuTypes.length; i++) {
            JSONArray billList = getBillListByInsuType(sessionId, fixmedinsCode, billDate, insuTypes[i], type);
            JSONArray newBillList = convertJSONArray(billList, insuTypes[i]);
            allBillList.addAll(newBillList);
        }
        return allBillList;
    }

    public JSONArray convertJSONArray(JSONArray orgList, String insutype) {
        JSONArray list = new JSONArray();
        for (int i = 0; i < orgList.size(); i++) {
            JSONObject jsonObject = orgList.getJSONObject(i);
            JSONObject newJsonObject = convertJSONObject(jsonObject, insutype);
            list.add(newJsonObject);
        }
        return list;
    }

    private JSONObject convertJSONObject(JSONObject jsonObject, String insutype) {
        JSONObject newJsonObject = new JSONObject();
        newJsonObject.put("setl_id", jsonObject.getString("setlId"));
        newJsonObject.put("mdtrt_id", jsonObject.getString("mdtrtId"));
        newJsonObject.put("psn_no", jsonObject.getString("psnNo"));
        newJsonObject.put("medfee_sumamt", jsonObject.getString("medfeeSumamt"));
        newJsonObject.put("bill_date", jsonObject.getString("setlTime"));
        newJsonObject.put("tran_type", jsonObject.getString("medfeeSumamt").contains("-") ? "2" : "1");
        newJsonObject.put("data_source", "2");
        newJsonObject.put("psn_name", jsonObject.getString("psnName"));
        newJsonObject.put("insutype", insutype);
        newJsonObject.put("medins_setl_id", jsonObject.getString("medinsSetlId"));
        newJsonObject.put("msgid", jsonObject.getString("medinsSetlId"));
        newJsonObject.put("med_type", jsonObject.getString("medType"));
        newJsonObject.put("certno", jsonObject.getString("certno"));


        return newJsonObject;
    }

    public String getBillDetail(String sessionId, String fixmedinsCode, String billDate, String insutype,
                                int pageNum, int pageSize, String type) {
        Map<String, Object> map = new HashMap<>();

        map.put("fixmedinsCode", fixmedinsCode);
        map.put("billDate", billDate);
        map.put("pageNum", pageNum);
        map.put("setlTime", billDate);
        map.put("insutype", insutype);
        map.put("pageSize", pageSize);
        map.put("_modulePartId_", "");
        String frontUrl = config.getFrontUrl();
        map.put("frontUrl", frontUrl);

        String url = config.getBillUrl();
        String token = config.getToken();
        String session = config.getSession();

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", "application/x-www-form-urlencoded");
        headerMap.put("X-XSRF-TOKEN", token);
        headerMap.put("Cookie", "XSRF-TOKEN=" + token + ";SESSION=" + session);

        log.info(sessionId + "调两定接口[" + insutype + "]入参:" + map.toString());

        String result = "";

        result = new HttpUtil().post(url, map, headerMap);


        log.info(sessionId + "调两定接口[" + insutype + "]出参:" + result);

        return result;
    }

    /*
     * 调用两定平台接口，获取文件导出结果
     */
    public JSONArray getBillDetailExport(String sessionId, String fixmedinsCode, String billDate, String insutype, String type)  {
        Map<String, Object> map = new HashMap<>();
        HttpUtil httpUtil = new HttpUtil();

        map.put("fixmedinsCode", fixmedinsCode != null ? fixmedinsCode : config.getFixmedinsCode());
        map.put("setlTime", billDate);
        map.put("insutype", insutype);
        map.put("_modulePartId_", "");
        map.put("frontUrl", config.getFrontUrl());

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Accept", "application/json, text/plain, */*");
        headerMap.put("Accept-Encoding", "gzip, deflate");
        headerMap.put("Connection", "keep-alive");
        headerMap.put("Content-Length", "165");
        headerMap.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        headerMap.put("Cookie", "XSRF-TOKEN=" + config.getToken() + "; SESSION=" + config.getSession());
        headerMap.put("Host", "mas.cq.hsip.gov.cn");
        headerMap.put("Origin", "http://mas.cq.hsip.gov.cn");
        headerMap.put("Referer", config.getFrontUrl().split("#")[0]);
        headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36");
        headerMap.put("X-XSRF-TOKEN", config.getToken());


        log.info("{} 发起调两定接口[{}]导出请求", sessionId, insutype);

        byte[] result = httpUtil.postExport(config.getExportUrl(), map, headerMap,sessionId);

        JSONArray jsonArray = null;

        if (result != null) {

            log.info("{} 导出Excel文件大小: {} 字节", sessionId, result.length);
            if(config.isSaveExcel()){
                // 2. 生成基础文件名
                String baseFileName = InsuType.nameOf(insutype) +""+ billDate;
                String extension = ".xlsx";

                HttpUtil.saveExcelFile(result, baseFileName,extension,sessionId);
            }

            // 配置驼峰转下划线
            SerializeConfig config = SerializeConfig.globalInstance;
            config.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;

            jsonArray = (JSONArray) JSON.toJSON(httpUtil.readExcelFile(result,insutype,sessionId));
        }

        log.info("{} 调两定接口[{}]出参: {}", sessionId, insutype, jsonArray);
        return jsonArray;
    }
}
