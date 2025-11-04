package com.showexcel.constant;

import java.text.SimpleDateFormat;
import java.util.Date;

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
            "应交报表数（1）", "前日暂收款（2）", "实交报表数（3）=（1）-（2）",
            "当日暂收款（4）", "实收现金数（5）=(3)+（4）", "留存数差额（6）=（7）-（3）-（8）",
            "留存现金数（7）", "备用金（8）", "备注"
    };

    //会计类型 1:现金统计表
    public static final Integer ACCOUNTING_STATISTICS_TYPE = 0;
    public static final String ACCOUNTING_STATISTICS_NAME = "会计室计表";

    //预约类型 1:预约统计表
    public static final Integer APPOINTMENT_STATISTICS_TYPE = 1;
    public static final String APPOINTMENT_STATISTICS_NAME = "预约统计表";

    //合计类型 2:合计统计表
    public static final Integer ALL_STATISTICS_TYPE = 2;
    public static final String ALL_STATISTICS_NAME = "合计统计表";

}
