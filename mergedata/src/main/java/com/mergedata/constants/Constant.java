package com.mergedata.constants;


import java.util.Arrays;
import java.util.List;

/**
 * 常量类
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
    public static final Integer SUCCESS = 1;
    /**
     * 是否成功：失败
     */
    public static final Integer FAILURE = 0;
    /*
     *  是否标识：肯定
     */
    public static final String YES = "1";

    /*
     *  是否标识：否定
     */
    public static final String NO = "0";


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
     * 节假日后第一天 周一
     */
    public static final String HOLIDAY_AFTER = "2";
    /*
     * 节假日前一天 周五
     */
    public static final String  HOLIDAY_PRE= "3";

    /*
     * 月末最后一天且是节假日
     */
    public static final String  HOLIDAY_MONTH_LASTDAY= "4";

    /*
     * 月末最后一天且 且 非假日 需要特殊计算 但不汇总
     */
    public static final String  HOLIDAY_NOT_MONTH_LASTDAY= "5";


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

    //===============门诊现金报表汇总标题========================
    /*
     * 汇总标题
     */
    public static final String OUTP_HOLIDAY_TOTAL_TITLE = "门诊现金汇总统计表";

    /*
     * 非汇总标题
     */
    public static final String HOLIDAY_NOT_TOTAL_TITLE = "门诊现金每日统计表";

    /*
     * 月初标题
     */
    public static final String HOLIDAY_MONTH_FIRST_TITLE = "门诊现金月初统计表";

    // 用于过滤掉不是操作员的行
    public static final List<String> EXCLUDE_OPERATOR_NAMES = Arrays.asList(
            "合计", "当日暂收款", "日报表数", "合计存款金额",
            "住院部当日借款", "住院部当日回款", "门诊当日借款",
            "门诊当日回款", "门诊当日实存金额", "审核："
    );


    // 用于过滤掉不是操作员的行
    public static final List<String> EXCLUDE_DOWN_NAMES = Arrays.asList(
            "当日暂收款", "日报表数", "合计存款金额",
            "住院部当日借款", "住院部当日回款", "门诊当日借款",
            "门诊当日回款", "门诊当日实存金额"
    );



    //===============月初数据/汇总/正常========================

    /*
     * 非汇总数据
     */
    public static final String NOT_TOTAL = "0";
    /*
     * 汇总数据
     */
    public static final String TOTAL = "1";
    /*
     * 初月数据
     */
    public static final String MONTH_FIRST = "2";
}
