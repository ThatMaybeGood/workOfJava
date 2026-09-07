package com.reports.annotation;

import java.lang.annotation.*;

/**
 * ETL 按需补数触发注解
 * <p>
 * 标注在 mapper 的“整表业务主查询方法”上。当该查询方法返回结果为空时，
 * 由 AOP 切面 {@link com.reports.aspect.EtlTriggerAspect} 解析本注解，
 * 触发对应的 ETL 任务抽数，使后续查询能够读到补数后的数据。
 *
 * <p>示例：
 * <pre>
 *   &#64;EtlTask(taskToken = EtlTaskConst.OUTP_FINANCE_PIE)
 *   List&lt;OutpFinancePieVO&gt; selectFinancePie(@Param("startDate") String startDate);
 * </pre>
 *
 * @see com.reports.constant.EtlTaskConst
 * @see com.reports.aspect.EtlTriggerAspect
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EtlTask {

    /**
     * ETL 任务调用 ID
     * <p>
     * 对应 ETL 平台「编辑任务 → 开启外部调用」后生成的任务调用 ID，
     * 触发抽数时用于唯一定位目标 ETL 任务。
     * 建议引用 {@link com.reports.constant.EtlTaskConst} 中的常量，避免魔法字符串散落。
     */
    String taskToken();

    /**
     * 透传给 ETL vars 的入参名白名单
     * <p>
     * 数组元素对应 mapper 方法上的 &#64;Param 名称；仅白名单内的入参会被透传
     * 到 ETL 任务的 vars 变量中。空数组表示不过滤（全部入参透传）。
     */
    String[] params() default {};

    /**
     * 透传参数的日期格式化声明（可选）
     * <p>
     * 每项形如 {@code "参数名=yyyy-MM-dd"}：把名为该参数的 {@link java.util.Date} 实参，
     * 在透传给 ETL vars 前按给定格式转成字符串。未声明该参数、或实参不是 Date 时忽略。
     * 缺省 = 不转换（Date 原样透传，JSON 序列化时为毫秒时间戳）。
     */
    String[] dateFormats() default {};

}
