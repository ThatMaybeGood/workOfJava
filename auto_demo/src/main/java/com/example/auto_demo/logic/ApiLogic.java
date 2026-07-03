package com.example.auto_demo.logic;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.example.auto_demo.config.AppConfig;
import com.example.auto_demo.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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

        Log.info("读取参数配置文件:" + config.toString());
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

        public JSONArray getSetlListByInsuType(String setlId, String fixmedinsCode, String insutype, String psnNo,String isFilter) {

        int pageNum = 1;

        Log.info("读取参数配置文件:" + config.toString());
        int pageSize = Integer.valueOf(config.getPageSize());

        String result = null;

        result = getSetllDetail(insutype, pageNum, pageSize, psnNo);

        JSONObject jsonObject = JSONObject.parseObject(result);
        JSONObject data = jsonObject.getJSONObject("data");
        JSONObject pageBean = data.getJSONObject("pageBean");
        JSONArray billList = pageBean.getJSONArray("data");

        JSONArray filteredList = new JSONArray();

            for (int i = 0; i < billList.size(); i++) {
                JSONObject item = billList.getJSONObject(i);
                if(fixmedinsCode.equals(item.getString("fixmedinsCode"))) {
                    if ("1".equals(isFilter)) {
                        if (setlId.equals(item.getString("setlId"))) {
                            filteredList.add(item);
                        }
                    }else {
                        filteredList.add(item);
                    }
                }
            }



        return filteredList;
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

    public JSONArray getSetlList(String setlId, String fixmedinsCode, String billDate, String insutype, String psnNo,String isFilter) {

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

            JSONArray billList = getSetlListByInsuType(setlId, fixmedinsCode, insuTypes[i], psnNo,isFilter);
            Log.info("调两定单边原始接口出参:" + billList.toString());

            JSONArray newBillList = convertSetlJSONArray(billList, insuTypes[i]);

            allBillList.addAll(newBillList);
        }
        return allBillList;
    }

    public JSONArray convertSetlJSONArray(JSONArray orgList, String insutype) {
        JSONArray list = new JSONArray();
        for (int i = 0; i < orgList.size(); i++) {
            JSONObject jsonObject = orgList.getJSONObject(i);
            JSONObject newJsonObject = convertSetlJSONObject(jsonObject, insutype);
            list.add(newJsonObject);
        }
        return list;
    }

    // ========== 工具方法：安全获取JSON字段 ==========
    private String s(JSONObject jsonObject, String key) {
        String val = jsonObject.getString(key);
        return val != null ? val : "";
    }

    private String s0(JSONObject jsonObject, String key) {
        String val = jsonObject.getString(key);
        return val != null ? val : "0";
    }

    private Double d(JSONObject jsonObject, String key) {
        try {
            return jsonObject.getDouble(key);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private JSONObject convertSetlJSONObject(JSONObject jsonObject, String insutype) {
        JSONObject newJsonObject = new JSONObject();

        // ========== 你原有的字段 ==========
        newJsonObject.put("setl_id", s(jsonObject, "setlId"));
        newJsonObject.put("mdtrt_id", s(jsonObject, "mdtrtId"));
        newJsonObject.put("psn_no", s(jsonObject, "psnNo"));
        newJsonObject.put("bill_date", s(jsonObject, "setlTime"));

        String medfeeSumamt = s0(jsonObject, "medfeeSumamt");
        newJsonObject.put("tran_type", medfeeSumamt.contains("-") ? "2" : "1");

        newJsonObject.put("data_source", "2");
        newJsonObject.put("psn_name", s(jsonObject, "psnName"));
        newJsonObject.put("insutype", insutype != null ? insutype : "");
        newJsonObject.put("medins_setl_id", s(jsonObject, "medinsSetlId"));
        newJsonObject.put("msgid", s(jsonObject, "medinsSetlId"));

        String medType = s(jsonObject, "medType");
        newJsonObject.put("med_type", medType.isEmpty() ? "" : MedType.nameOf(medType));
        newJsonObject.put("med_type_code", medType);
        newJsonObject.put("card_no", s(jsonObject, "certno"));
        newJsonObject.put("fixmedins_code", s(jsonObject, "fixmedinsCode"));
        newJsonObject.put("fixmedins_name", s(jsonObject, "fixmedinsName"));
        newJsonObject.put("medfee_sumamt", s0(jsonObject, "medfeeSumamt"));
        newJsonObject.put("fulamt_ownpay_amt", s0(jsonObject, "fulamtOwnpayAmt"));
        newJsonObject.put("inscp_amt", s0(jsonObject, "inscpAmt"));
        newJsonObject.put("crt_dedc", s0(jsonObject, "crtDedc"));
        newJsonObject.put("act_pay_dedc", s0(jsonObject, "actPayDedc"));
        newJsonObject.put("hi_agre_sumfee", s0(jsonObject, "hiAgreSumfee"));
        newJsonObject.put("hifp_pay", s0(jsonObject, "fundPaySumamt"));
        newJsonObject.put("fund_pay_sumamt", s0(jsonObject, "hifpPay"));
        newJsonObject.put("psn_pay", s0(jsonObject, "psnPay"));
        newJsonObject.put("acct_pay", s0(jsonObject, "acctPay"));

        // ========== 补全的字段 ==========
        // 人员信息
        newJsonObject.put("psn_insu_rlts_id", s(jsonObject, "psnInsuRltsId"));
        newJsonObject.put("insu_admdvs", s(jsonObject, "insuAdmdvs"));
        newJsonObject.put("psn_cert_type", s(jsonObject, "psnCertType"));
        newJsonObject.put("certno", s(jsonObject, "certno"));
        newJsonObject.put("gend", s(jsonObject, "gend"));
        newJsonObject.put("naty", s(jsonObject, "naty"));
        newJsonObject.put("brdy", s(jsonObject, "brdy"));
        newJsonObject.put("age", d(jsonObject, "age"));
        newJsonObject.put("psn_type", s(jsonObject, "psnType"));
        newJsonObject.put("cvlserv_flag", s0(jsonObject, "cvlservFlag"));
        newJsonObject.put("cvlserv_lv", s(jsonObject, "cvlservLv"));
        newJsonObject.put("sp_psn_type", s(jsonObject, "spPsnType"));
        newJsonObject.put("sp_psn_type_lv", s(jsonObject, "spPsnTypeLv"));
        newJsonObject.put("clct_grde", s(jsonObject, "clctGrde"));
        newJsonObject.put("flxempe_flag", s0(jsonObject, "flxempeFlag"));
        newJsonObject.put("nwb_flag", s(jsonObject, "nwbFlag"));

        // 单位信息
        newJsonObject.put("emp_no", s(jsonObject, "empNo"));
        newJsonObject.put("emp_name", s(jsonObject, "empName"));
        newJsonObject.put("emp_type", s(jsonObject, "empType"));
        newJsonObject.put("econ_type", s(jsonObject, "econType"));
        newJsonObject.put("afil_indu", s(jsonObject, "afilIndu"));
        newJsonObject.put("afil_rlts", s(jsonObject, "afilRlts"));
        newJsonObject.put("emp_mgt_type", s(jsonObject, "empMgtType"));
        newJsonObject.put("pay_loc", s(jsonObject, "payLoc"));

        // 医院信息
        newJsonObject.put("hosp_lv", s(jsonObject, "hospLv"));
        newJsonObject.put("fix_blng_admdvs", s(jsonObject, "fixBlngAdmdvs"));
        newJsonObject.put("lmtpric_hosp_lv", s(jsonObject, "lmtpricHospLv"));
        newJsonObject.put("dedc_hosp_lv", s(jsonObject, "dedcHospLv"));

        // 时间日期
        newJsonObject.put("begndate", s(jsonObject, "begndate"));
        newJsonObject.put("enddate", s(jsonObject, "enddate"));
        newJsonObject.put("setl_time", s(jsonObject, "setlTime"));
        newJsonObject.put("begntime", s(jsonObject, "begntime"));
        newJsonObject.put("endtime", s(jsonObject, "endtime"));
        newJsonObject.put("updt_time", s(jsonObject, "updtTime"));
        newJsonObject.put("crte_time", s(jsonObject, "crteTime"));
        newJsonObject.put("opt_time", s(jsonObject, "optTime"));

        // 业务类型/状态
        newJsonObject.put("mdtrt_cert_type", s(jsonObject, "mdtrtCertType"));
        newJsonObject.put("setl_type", s(jsonObject, "setlType"));
        newJsonObject.put("clr_type", s(jsonObject, "clrType"));
        newJsonObject.put("clr_way", s(jsonObject, "clrWay"));
        newJsonObject.put("clr_optins", s(jsonObject, "clrOptins"));
        newJsonObject.put("refd_setl_flag", s0(jsonObject, "refdSetlFlag"));
        newJsonObject.put("mid_setl_flag", s(jsonObject, "midSetlFlag"));
        newJsonObject.put("acct_used_flag", s(jsonObject, "acctUsedFlag"));
        newJsonObject.put("vali_flag", s(jsonObject, "valiFlag"));
        newJsonObject.put("rchk_flag", s(jsonObject, "rchkFlag"));
        newJsonObject.put("reim_stas", s(jsonObject, "reimStas"));
        newJsonObject.put("psn_setlway", s(jsonObject, "psnSetlway"));

        // 金额补全
        newJsonObject.put("overlmt_selfpay", s0(jsonObject, "overlmtSelfpay"));
        newJsonObject.put("preselfpay_amt", s0(jsonObject, "preselfpayAmt"));
        newJsonObject.put("dedc_std", s0(jsonObject, "dedcStd"));
        newJsonObject.put("pool_prop_selfpay", s0(jsonObject, "poolPropSelfpay"));
        newJsonObject.put("cvlserv_pay", s0(jsonObject, "cvlservPay"));
        newJsonObject.put("hifes_pay", s0(jsonObject, "hifesPay"));
        newJsonObject.put("hifmi_pay", s0(jsonObject, "hifmiPay"));
        newJsonObject.put("hifob_pay", s0(jsonObject, "hifobPay"));
        newJsonObject.put("hifdm_pay", s0(jsonObject, "hifdmPay"));
        newJsonObject.put("maf_pay", s0(jsonObject, "mafPay"));
        newJsonObject.put("othfund_pay", s0(jsonObject, "othfundPay"));
        newJsonObject.put("cash_payamt", s0(jsonObject, "cashPayamt"));
        newJsonObject.put("ownpay_hosp_part", s0(jsonObject, "ownpayHospPart"));

        // 其他字段
        newJsonObject.put("year", s(jsonObject, "year"));
        newJsonObject.put("balc", d(jsonObject, "balc"));
        newJsonObject.put("mdtrt_cert_no", s(jsonObject, "mdtrtCertNo"));
        newJsonObject.put("rid", s(jsonObject, "rid"));
        newJsonObject.put("crter_id", s(jsonObject, "crterId"));
        newJsonObject.put("crter_name", s(jsonObject, "crterName"));
        newJsonObject.put("crte_optins_no", s(jsonObject, "crteOptinsNo"));
        newJsonObject.put("opter_id", s(jsonObject, "opterId"));
        newJsonObject.put("opter_name", s(jsonObject, "opterName"));
        newJsonObject.put("optins_no", s(jsonObject, "optinsNo"));
        newJsonObject.put("poolarea_no", s(jsonObject, "poolareaNo"));
        newJsonObject.put("evtsn", s(jsonObject, "evtsn"));
        newJsonObject.put("serv_matt_inst_id", s(jsonObject, "servMattInstId"));
        newJsonObject.put("serv_matt_node_inst_id", s(jsonObject, "servMattNodeInstId"));
        newJsonObject.put("evt_inst_id", s(jsonObject, "evtInstId"));
        newJsonObject.put("evt_type", s(jsonObject, "evtType"));
        newJsonObject.put("med_fee_reg_id", s(jsonObject, "medFeeRegId"));
        newJsonObject.put("manl_reim_rea", s(jsonObject, "manlReimRea"));
        newJsonObject.put("dfr_obj", s(jsonObject, "dfrObj"));
        newJsonObject.put("bankcode", s(jsonObject, "bankcode"));
        newJsonObject.put("bank_type_code", s(jsonObject, "bankTypeCode"));
        newJsonObject.put("bankacct", s(jsonObject, "bankacct"));
        newJsonObject.put("acctname", s(jsonObject, "acctname"));
        newJsonObject.put("bank_samecity_out_flag", s(jsonObject, "bankSamecityOutFlag"));
        newJsonObject.put("cal_ipt_cnt", s(jsonObject, "calIptCnt"));
        newJsonObject.put("att_val", s(jsonObject, "attVal"));
        newJsonObject.put("invono", s(jsonObject, "invono"));
        newJsonObject.put("sumfee", s(jsonObject, "sumfee"));
        newJsonObject.put("init_setl_id", s(jsonObject, "initSetlId"));
        newJsonObject.put("dise_no", s(jsonObject, "diseNo"));
        newJsonObject.put("dise_name", s(jsonObject, "diseName"));
        newJsonObject.put("memo", s(jsonObject, "memo"));
        newJsonObject.put("setl_cashpay_way", s(jsonObject, "setlCashpayWay"));
        newJsonObject.put("acct_mulaid_pay", s(jsonObject, "acctMulaidPay"));

        return newJsonObject;
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
        newJsonObject.put("med_type", MedType.nameOf(jsonObject.getString("medType")));  //返回医疗类别的中文
        newJsonObject.put("med_type_code", jsonObject.getString("medType"));
        newJsonObject.put("card_no", jsonObject.getString("certno"));

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

        Log.info("调两定接口[" + insutype + "]入参:" + map.toString());

        String result = "";

        result = new HttpUtil().post(url, map, headerMap);


//        Log.info("调两定接口[" + insutype + "]出参:" + result);

        return result;
    }


    public String getSetllDetail( String insutype,int pageNum, int pageSize, String psnNo) {
        Map<String, Object> map = new HashMap<>();

        map.put("psnNo", psnNo);
        map.put("pageNum", pageNum);
        map.put("pageSize", pageSize);
        map.put("insutype", insutype);
        map.put("_modulePartId_", "17-2-4");
        map.put("frontUrl", config.getFrontSetlUrl());

        String url = config.getSetlUrl();
        String token = config.getToken();
        String session = config.getSession();

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", "application/x-www-form-urlencoded");
        headerMap.put("X-XSRF-TOKEN", token);
        headerMap.put("Cookie", "XSRF-TOKEN=" + token + ";SESSION=" + session);

        Log.info("调两定接口[" + insutype + "]入参:" + map.toString());

        String result = "";

        result = new HttpUtil().post(url, map, headerMap);


//        Log.info("调两定接口[" + insutype + "]出参:" + result);

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


        Log.info("{} 发起调两定接口[{}]导出请求", sessionId, insutype);

        byte[] result = httpUtil.postExport(config.getExportUrl(), map, headerMap,sessionId);

        JSONArray jsonArray = null;

        if (result != null) {

            Log.info("{} 导出Excel文件大小: {} 字节", sessionId, result.length);
            if(config.isSaveExcel()){
                // 2. 生成基础文件名
                String baseFileName = InsuType.nameOf(insutype) +" "+ billDate;
                String extension = ".xlsx";

                HttpUtil.saveExcelFile(result, baseFileName,extension,sessionId);
            }

            // 配置驼峰转下划线
            SerializeConfig config = SerializeConfig.globalInstance;
            config.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;

            jsonArray = (JSONArray) JSON.toJSON(httpUtil.readExcelFile(result,insutype,sessionId));
        }

        Log.info("{} 调两定接口[{}]出参: {}", sessionId, insutype, jsonArray);
        return jsonArray;
    }
}
