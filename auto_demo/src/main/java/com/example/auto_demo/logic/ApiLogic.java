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

    /**
     * 转换结算单接口返回的JSON对象
     *
     * @param jsonObject
     * @return
     */
    private JSONObject convertSetlJSONObject(JSONObject jsonObject, String insutype) {
        JSONObject newJsonObject = new JSONObject();

        // ========== 基础结算信息 ==========
        newJsonObject.put("setl_id", s(jsonObject, "setlId")); // 结算ID，医保系统内部唯一标识
        newJsonObject.put("mdtrt_id", s(jsonObject, "mdtrtId")); // 就诊流水号，一次就诊唯一标识
        newJsonObject.put("psn_no", s(jsonObject, "psnNo")); // 个人编号，参保人唯一标识
        newJsonObject.put("bill_date", s(jsonObject, "setlTime")); // 账单日期，取自结算时间

        // 判断交易类型：医疗总费用含负号为退款/冲正(2)，否则为正常交易(1)
        String medfeeSumamt = s0(jsonObject, "medfeeSumamt");
        newJsonObject.put("tran_type", medfeeSumamt.contains("-") ? "2" : "1");

        newJsonObject.put("data_source", "2"); // 数据来源，固定值"2"（具体含义视业务约定）
        newJsonObject.put("psn_name", s(jsonObject, "psnName")); // 参保人姓名
        newJsonObject.put("insutype", insutype != null ? insutype : ""); // 险种类型（如职工医保/居民医保）
        newJsonObject.put("medins_setl_id", s(jsonObject, "medinsSetlId")); // 医疗机构结算ID
        newJsonObject.put("msgid", s(jsonObject, "medinsSetlId")); // 消息ID，复用医疗机构结算ID

        // 医疗类别：原始代码转译为中文名称，保留原始代码
        String medType = s(jsonObject, "medType");
        newJsonObject.put("med_type", medType.isEmpty() ? "" : MedType.nameOf(medType)); // 医疗类别名称
        newJsonObject.put("med_type_code", medType); // 医疗类别代码
        newJsonObject.put("card_no", s(jsonObject, "certno")); // 卡号/证件号
        newJsonObject.put("fixmedins_code", s(jsonObject, "fixmedinsCode")); // 定点医疗机构代码
        newJsonObject.put("fixmedins_name", s(jsonObject, "fixmedinsName")); // 定点医疗机构名称

        // ========== 金额相关 ==========
        newJsonObject.put("fulamt_ownpay_amt", s0(jsonObject, "fulamtOwnpayAmt")); // 全自费金额
        newJsonObject.put("inscp_amt", s0(jsonObject, "inscpAmt")); // 统筹基金支付金额
        newJsonObject.put("crt_dedc", s0(jsonObject, "crtDedc")); // 起付线金额
        newJsonObject.put("act_pay_dedc", s0(jsonObject, "actPayDedc")); // 实际支付起付线金额
        newJsonObject.put("hi_agre_sumfee", s0(jsonObject, "hiAgreSumfee")); // 医保政策范围内费用
        newJsonObject.put("fund_pay_sumamt", s0(jsonObject, "fundPaySumamt")); // 基金支付总额
        newJsonObject.put("psn_pay", s0(jsonObject, "psnPay")); // 账户支付额（含现金+账户）

        // ========== 补充金额字段 ==========
        newJsonObject.put("ownpay_hosp_part", s0(jsonObject, "ownpayHospPart")); // 个人自付医疗费用
        newJsonObject.put("overlmt_selfpay", s0(jsonObject, "overlmtSelfpay")); // 超限价自费金额
        newJsonObject.put("preselfpay_amt", s0(jsonObject, "preselfpayAmt")); // 政策范围外自费金额
        newJsonObject.put("dedc_std", s0(jsonObject, "dedcStd")); // 起付线标准
        newJsonObject.put("pool_prop_selfpay", s0(jsonObject, "poolPropSelfpay")); // 统筹比例自付金额
        newJsonObject.put("hifdm_pay", s0(jsonObject, "hifdmPay")); // 生育保险基金支付

        // ===============需要上报的金额字段 ==========
        newJsonObject.put("medfee_sumamt", s0(jsonObject, "medfeeSumamt")); // 医疗费用总金额     ！！！
        newJsonObject.put("acct_pay", s0(jsonObject, "acctPay")); // 个人账户支出   !!!
        newJsonObject.put("hlfp_pay", s0(jsonObject, "hifpPay")); // 统筹基金出 ！！！
        newJsonObject.put("cash_payamt", s0(jsonObject, "cashPayamt")); // 现金支出    !!!
        newJsonObject.put("cvlserv_pay", s0(jsonObject, "cvlservPay")); // 公务员补助金支出  !!!
        newJsonObject.put("hlfes_pay", s0(jsonObject, "hifesPay")); // 企业补充医疗保险基金支出     !!!
        newJsonObject.put("hlfmi_pay", s0(jsonObject, "hifmiPay")); // 居民大病支出     !!!
        newJsonObject.put("hlfob_pay", s0(jsonObject, "hifobPay")); // 职工大病支出     !!!
        newJsonObject.put("maf_pay", s0(jsonObject, "mafPay")); // 医疗救助支出        !!!
        newJsonObject.put("othfund_pay", s0(jsonObject, "othfundPay")); // 其他支出       !!!


        // ========== 人员信息 ==========
        newJsonObject.put("psn_insu_rlts_id", s(jsonObject, "psnInsuRltsId")); // 个人与单位参保关系ID
        newJsonObject.put("insu_admdvs", s(jsonObject, "insuAdmdvs")); // 参保地医保行政区划
        newJsonObject.put("psn_cert_type", s(jsonObject, "psnCertType")); // 证件类型（如身份证）
        newJsonObject.put("certno", s(jsonObject, "certno")); // 证件号码
        newJsonObject.put("gend", s(jsonObject, "gend")); // 性别
        newJsonObject.put("naty", s(jsonObject, "naty")); // 民族
        newJsonObject.put("brdy", s(jsonObject, "brdy")); // 出生日期
        newJsonObject.put("age", d(jsonObject, "age")); // 年龄
        newJsonObject.put("psn_type", s(jsonObject, "psnType")); // 人员类别（职工/居民等）
        newJsonObject.put("cvlserv_flag", s0(jsonObject, "cvlservFlag")); // 公务员标识（0-否 1-是）
        newJsonObject.put("cvlserv_lv", s(jsonObject, "cvlservLv")); // 公务员等级
        newJsonObject.put("sp_psn_type", s(jsonObject, "spPsnType")); // 特殊人员类型
        newJsonObject.put("sp_psn_type_lv", s(jsonObject, "spPsnTypeLv")); // 特殊人员类型等级
        newJsonObject.put("clct_grde", s(jsonObject, "clctGrde")); // 征收等级
        newJsonObject.put("flxempe_flag", s0(jsonObject, "flxempeFlag")); // 灵活就业标识
        newJsonObject.put("nwb_flag", s(jsonObject, "nwbFlag")); // 新业态标识

        // ========== 单位/雇主信息 ==========
        newJsonObject.put("emp_no", s(jsonObject, "empNo")); // 单位编号
        newJsonObject.put("emp_name", s(jsonObject, "empName")); // 单位名称
        newJsonObject.put("emp_type", s(jsonObject, "empType")); // 单位类型
        newJsonObject.put("econ_type", s(jsonObject, "econType")); // 经济类型
        newJsonObject.put("afil_indu", s(jsonObject, "afilIndu")); // 所属行业
        newJsonObject.put("afil_rlts", s(jsonObject, "afilRlts")); // 隶属关系
        newJsonObject.put("emp_mgt_type", s(jsonObject, "empMgtType")); // 单位管理类型
        newJsonObject.put("pay_loc", s(jsonObject, "payLoc")); // 缴费地区

        // ========== 医院/机构信息 ==========
        newJsonObject.put("hosp_lv", s(jsonObject, "hospLv")); // 医院等级
        newJsonObject.put("fix_blng_admdvs", s(jsonObject, "fixBlngAdmdvs")); // 定点机构所属地区
        newJsonObject.put("lmtpric_hosp_lv", s(jsonObject, "lmtpricHospLv")); // 限价医院等级
        newJsonObject.put("dedc_hosp_lv", s(jsonObject, "dedcHospLv")); // 起付线医院等级

        // ========== 时间日期 ==========
        newJsonObject.put("begndate", s(jsonObject, "begndate")); // 开始日期
        newJsonObject.put("enddate", s(jsonObject, "enddate")); // 结束日期
        newJsonObject.put("setl_time", s(jsonObject, "setlTime")); // 结算时间
        newJsonObject.put("begntime", s(jsonObject, "begntime")); // 开始时间
        newJsonObject.put("endtime", s(jsonObject, "endtime")); // 结束时间
        newJsonObject.put("updt_time", s(jsonObject, "updtTime")); // 更新时间
        newJsonObject.put("crte_time", s(jsonObject, "crteTime")); // 创建时间
        newJsonObject.put("opt_time", s(jsonObject, "optTime")); // 操作时间

        // ========== 业务类型/状态标识 ==========
        newJsonObject.put("mdtrt_cert_type", s(jsonObject, "mdtrtCertType")); // 就诊凭证类型（如电子凭证）
        newJsonObject.put("setl_type", s(jsonObject, "setlType")); // 结算类型
        newJsonObject.put("clr_type", s(jsonObject, "clrType")); // 清算类别
        newJsonObject.put("clr_way", s(jsonObject, "clrWay")); // 清算方式
        newJsonObject.put("clr_optins", s(jsonObject, "clrOptins")); // 清算经办机构
        newJsonObject.put("refd_setl_flag", s0(jsonObject, "refdSetlFlag")); // 转诊结算标识
        newJsonObject.put("mid_setl_flag", s(jsonObject, "midSetlFlag")); // 中途结算标识
        newJsonObject.put("acct_used_flag", s(jsonObject, "acctUsedFlag")); // 账户使用标识
        newJsonObject.put("vali_flag", s(jsonObject, "valiFlag")); // 数据有效性标识
        newJsonObject.put("rchk_flag", s(jsonObject, "rchkFlag")); // 复核标识
        newJsonObject.put("reim_stas", s(jsonObject, "reimStas")); // 报销状态
        newJsonObject.put("psn_setlway", s(jsonObject, "psnSetlway")); // 个人结算方式


        // ========== 其他扩展字段 ==========
        newJsonObject.put("year", s(jsonObject, "year")); // 年份
        newJsonObject.put("balc", d(jsonObject, "balc")); // 余额（可能是账户或基金余额）
        newJsonObject.put("mdtrt_cert_no", s(jsonObject, "mdtrtCertNo")); // 就诊凭证编号
        newJsonObject.put("rid", s(jsonObject, "rid")); // 数据库记录ID
        newJsonObject.put("crter_id", s(jsonObject, "crterId")); // 创建人ID
        newJsonObject.put("crter_name", s(jsonObject, "crterName")); // 创建人姓名
        newJsonObject.put("crte_optins_no", s(jsonObject, "crteOptinsNo")); // 创建经办机构编号
        newJsonObject.put("opter_id", s(jsonObject, "opterId")); // 操作人ID
        newJsonObject.put("opter_name", s(jsonObject, "opterName")); // 操作人姓名
        newJsonObject.put("optins_no", s(jsonObject, "optinsNo")); // 操作经办机构编号
        newJsonObject.put("poolarea_no", s(jsonObject, "poolareaNo")); // 统筹区编号
        newJsonObject.put("evtsn", s(jsonObject, "evtsn")); // 事件流水号
        newJsonObject.put("serv_matt_inst_id", s(jsonObject, "servMattInstId")); // 业务办理实例ID
        newJsonObject.put("serv_matt_node_inst_id", s(jsonObject, "servMattNodeInstId")); // 业务办理节点实例ID
        newJsonObject.put("evt_inst_id", s(jsonObject, "evtInstId")); // 事件实例ID
        newJsonObject.put("evt_type", s(jsonObject, "evtType")); // 事件类型
        newJsonObject.put("med_fee_reg_id", s(jsonObject, "medFeeRegId")); // 医疗费用登记ID
        newJsonObject.put("manl_reim_rea", s(jsonObject, "manlReimRea")); // 手工报销原因
        newJsonObject.put("dfr_obj", s(jsonObject, "dfrObj")); // 拒付对象
        newJsonObject.put("bankcode", s(jsonObject, "bankcode")); // 银行代码
        newJsonObject.put("bank_type_code", s(jsonObject, "bankTypeCode")); // 银行类型代码
        newJsonObject.put("bankacct", s(jsonObject, "bankacct")); // 银行账号
        newJsonObject.put("acctname", s(jsonObject, "acctname")); // 账户名称
        newJsonObject.put("bank_samecity_out_flag", s(jsonObject, "bankSamecityOutFlag")); // 银行同城/异地标识
        newJsonObject.put("cal_ipt_cnt", s(jsonObject, "calIptCnt")); // 结算住院次数
        newJsonObject.put("att_val", s(jsonObject, "attVal")); // 附件值（可能存放额外信息）
        newJsonObject.put("invono", s(jsonObject, "invono")); // 发票号
        newJsonObject.put("sumfee", s(jsonObject, "sumfee")); // 汇总费用（可能与总费用不同）
        newJsonObject.put("init_setl_id", s(jsonObject, "initSetlId")); // 初始结算ID（用于关联）
        newJsonObject.put("dise_no", s(jsonObject, "diseNo")); // 疾病编码
        newJsonObject.put("dise_name", s(jsonObject, "diseName")); // 疾病名称
        newJsonObject.put("memo", s(jsonObject, "memo")); // 备注
        newJsonObject.put("setl_cashpay_way", s(jsonObject, "setlCashpayWay")); // 结算现金支付方式
        newJsonObject.put("acct_mulaid_pay", s(jsonObject, "acctMulaidPay")); // 账户多协助支付

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
