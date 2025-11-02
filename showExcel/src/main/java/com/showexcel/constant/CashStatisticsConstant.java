package com.showexcel.constant;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2025/10/31 11:35
 */
public class CashStatisticsConstant  {

    public static final String TITLE = "现金统计表";

    // 表头常量
    public static final String[] TABLE_HEADERS = {
            "序号", "名称", "预交金收入", "医疗收入", "挂号收入",
            "应交报表数", "前日暂收款", "实交报表数",
            "当日暂收款", "实收现金数", "留存数差额",
            "留存现金数", "备用金", "备注"
    };

    //会计类型 1:现金统计表
    public static final Integer CASH_STATISTICS_TYPE = 0;

    //预约类型 1:预约统计表
    public static final Integer APPOINTMENT_STATISTICS_TYPE = 1;

}
