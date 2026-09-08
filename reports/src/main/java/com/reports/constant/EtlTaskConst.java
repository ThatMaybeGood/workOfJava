package com.reports.constant;

/**
 * ETL 任务调用 ID 常量集中定义
 * <p>
 * 不同报表/任务各加一个常量；mapper 主查询方法上的 {@code @EtlTask} 注解
 * 通过 {@code EtlTaskConst.XXX} 引用，避免魔法字符串散落在各处。
 */
public final class EtlTaskConst {

    /** 门诊挂号表 TR_OUTP_FIN_CLINIC_MASTER 抽取任务：门诊量指标、bt1-bt3、bt8 挂号部分 */
    public static final String OUTP_FINANCE_CLINIC = "OUTP_FINANCE_CLINIC_REPLACE_TOKEN";

    /** 收据表 TR_OUTP_FIN_RCPT_ACCT 抽取任务：缴费人次指标、bt4、bt8 收据部分 */
    public static final String OUTP_FINANCE_RCPT = "OUTP_FINANCE_RCPT_REPLACE_TOKEN";

    /** 结账主表 TR_OUTP_FIN_ACCT_MASTER 抽取任务：收据张数/收入金额指标、bt6、bt11 */
    public static final String OUTP_FINANCE_ACCT = "OUTP_FINANCE_ACCT_REPLACE_TOKEN";

    /** 缴费支付表 TR_OUTP_FIN_PAYMENTS_MONEY 抽取任务：bt5、bt10 */
    public static final String OUTP_FINANCE_PAYMENTS_MONEY = "OUTP_FINANCE_PAYMENTS_MONEY_REPLACE_TOKEN";

    /** 结账支付表 TR_OUTP_FIN_ACCT_MONEY 抽取任务：bt7、bt9、bt12 */
    public static final String OUTP_FINANCE_ACCT_MONEY = "OUTP_FINANCE_ACCT_MONEY_REPLACE_TOKEN";

    /** 排队队列表 TR_OUTP_FIN_MOP_QUEUE 抽取任务：bt1 订单来源依赖 */
    public static final String OUTP_FINANCE_MOP_QUEUE = "OUTP_FINANCE_MOP_QUEUE_REPLACE_TOKEN";

    private EtlTaskConst() {
    }
}
