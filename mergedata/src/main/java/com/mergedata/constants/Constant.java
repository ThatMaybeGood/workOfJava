package com.mergedata.constants;


/**
 * 响应常量类
 */
public class Constant {
    /**
     * API响应状态码：失败
     */
    public static final String API_RESPONSE_FAILURE ="10001";
    /**
     * API响应状态码：成功
     */
    public static final String API_RESPONSE_SUCCESS ="10000";

    //===============API响应信息==================================
    /**
     * API响应信息：失败
     */
    public static final String API_RESPONSE_FAILURE_MESSAGE ="请求失败";
    /**
     * API响应信息：成功
     */
    public static final String API_RESPONSE_SUCCESS_MESSAGE ="请求成功";

    //====================是否成功或失败===================================
    /**
     * 是否成功：成功
     */
    public static final String SUCCESS = "1";
    /**
     * 是否成功：失败
     */
    public static final String FAILURE = "0";
    /*
     *  是否标识：肯定
     */
    public static final String FLAG_YES = "1";

    /*
     *  是否标识：否定
     */
    public static final String FLAG_NO = "0";


    //===================存储过程出参状态码和信息 游标=======================
    /*
     * 存储过程出参状态码名称
     */
    public static final String SP_OUT_CODE ="A_RETCODE";
    /*
     * 存储过程出参信息名称
     */
    public static final String SP_OUT_MESSAGE ="A_ERRMSG";
    /*
     * 存储过程出参游标名称
     */
    public static final String SP_OUT_CURSOR ="A_RESULTSET";

    //==========存储过程出参状态码值=====================
    /*
     * 存储过程出参状态码值
     */
    public static final int SP_SUCCESS = 1;
    /*
     * 存储过程出参状态码值
     */
    public static final int SP_FAILURE = -1;

    //===============存储过程类型========================
    /*
    * 存储过程类型：查询、插入、更新、删除
     */
    public static final String SP_TYPE_SELECT = "0";
    /*
    * 存储过程类型：插入
     */
    public static final String SP_TYPE_INSERT = "1";
    /*
    * 存储过程类型：更新
     */
    public static final String SP_TYPE_UPDATE = "2";
    /*
    * 存储过程类型：删除
     */
    public static final String SP_TYPE_DELETE = "3";

    //===============节假日类型========================
    /*
     * 正常工作日
     */
    public static final String HOLIDAY_NOT = "0";
    /*
     * 节假日
     */
    public static final String HOLIDAY_IS = "1";
    /*
     * 节假日后第一天
     */
    public static final String HOLIDAY_AFTER = "2";
    /*
     * 节假日前一天
     */
    public static final String  HOLIDAY_PRE= "3";

    //===============门诊/住院类型========================
    /*
     * 门诊
     */
    public static final String TYPE_OUTP = "0";
    /*
     * 住院
     */
    public static final String TYPE_INP = "1";

    //==============方法名=====================
    /*
     * 门诊现金报表收入方法名
     */
    public static final String HIS_METHOD_OUTP = "orgine.powermsp.service.overt.extend.SP_GetHisIncome_938";
    /*
     * 住院现金报表收入方法名
     */
    public static final String HIS_METHOD_INP = "orgine.powermsp.service.overt.extend.queryInpCashReport";

    //===============接口名称========================
    /*
     * 门诊现金报表接口名称
     */
    public static final String REPORT_NAME_OUTP = "门诊现金报表";
    /*
     * 住院现金报表接口名称
     */
    public static final String REPORT_NAME_INP = "住院现金报表";
}
