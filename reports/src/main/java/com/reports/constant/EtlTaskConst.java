package com.reports.constant;

/**
 * ETL 任务调用 ID 常量集中定义
 * <p>
 * 不同报表/任务各加一个常量；mapper 主查询方法上的 {@code @EtlTask} 注解
 * 通过 {@code EtlTaskConst.XXX} 引用，避免魔法字符串散落在各处。
 */
public final class EtlTaskConst {

    /**
     * 门诊财务报表 bt1 订单来源饼图对应的 ETL 任务调用 ID
     * <p>
     * （占位，需在 ETL 平台「编辑任务 → 开启外部调用」生成后替换为真实 taskToken）
     */
    public static final String OUTP_FINANCE_PIE = "OUTP_FINANCE_PIE_REPLACE_TOKEN";

    private EtlTaskConst() {
    }
}
