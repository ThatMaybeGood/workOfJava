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

    /**
     * 范围展开的起点参数名（{@code @Param} 名，须为 Date）：配合 {@link #rangeEnd} 使用。
     * <p>
     * 三者（rangeStart/rangeEnd/rangeTarget）都非空时启用「按天展开」：查询结果为空，
     * 不把起止日期透传给 ETL，而是把 [rangeStart, rangeEnd] 逐日拆分，
     * 每天以 {@code {rangeTarget: 当日}} 触发一次补数（去重按天生效）。
     * 缺省 = 空串，不展开。
     */
    String rangeStart() default "";

    /**
     * 范围展开的终点参数名（{@code @Param} 名，须为 Date），语义同 {@link #rangeStart}。
     */
    String rangeEnd() default "";

    /**
     * 展开后传给 ETL vars 的单日变量名。
     */
    String rangeTarget() default "";

    /**
     * 展开时日期格式（对 start/end 实参按此 pattern 解析出天数后逐日格式化）。
     */
    String rangePattern() default "yyyy-MM-dd";

    /**
     * 起点/终点实参的解析格式（默认 yyyy-MM-dd）。
     * <p>
     * 实参为 Date 时直接取日期部分；为 String 时按此 pattern 解析——
     * 先按 LocalDate 解析，失败再按 YearMonth（取月初）、Year（取年初）兜底；
     * 终点参数按 YearMonth/Year 解析时取月末/年末，保证整个月/年都被展开覆盖。
     * 解析失败时退化为整段触发（vars 仅含 rangeTarget=起点原值）。
     */
    String rangeParsePattern() default "yyyy-MM-dd";

    /**
     * 单次展开的最大天数（保护：防止超大范围刷爆触发线程池），超出按最大天数截断。
     */
    int rangeMaxDays() default 31;

}
