package com.mergedata.constants;

public class ReqConstant {
    // 现金存储过程名称
//    public static final String URL_API_HISINCOME ="http://168.168.235.88:18401/orgine/powermsp/service/overt";
    public static final String URL_API_HISINCOME ="http://localhost:8080/api/all";

    //中台方法名称
    public static final String METHOD_HISINCOME ="orgine.powermsp.service.overt.extend.SP_GetHisIncome_938";



    //API响应状态码
    public static final String API_RESPONSE_FAILURE ="10001";
    public static final String API_RESPONSE_SUCCESS ="10000";

    //存储过程出参状态码和信息 游标
    /*
     * 存储过程出参状态码名称
     */
    public static final String SP_OUT_CODE ="A_CODE";
    /*
     * 存储过程出参信息名称
     */
    public static final String SP_OUT_MESSAGE ="A_MESSAGE";
    /*
     * 存储过程出参游标名称
     */
    public static final String SP_OUT_CURSOR ="A_CURSOR";
    /*
     * 存储过程出参状态码值
     */
    public static final int SP_SUCCESS = 1;
    /*
     * 存储过程出参状态码值
     */
    public static final int SP_FAILURE = -1;


}
