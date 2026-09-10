package com.reports.aspect;

import com.reports.annotation.EtlTask;
import com.reports.config.DynamicDataSourceContextHolder;
import com.reports.service.EtlClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * ETL 补数触发切面
 * <p>
 * 拦截带有 {@link EtlTask} 注解的方法：当查询返回空结果（空集合 / 空 Map / 空 Optional / 空数组）时，
 * 尽力调用 {@link EtlClient} 的 ensure 方法触发 ETL 补数，随后仍将原始空结果返回给调用方。
 * <p>
 * Order 为 0，晚于 {@link DataSourceAspect}（Order=-1）。外层切面先完成数据源切换，
 * 本切面更靠近 proceed，保证 proceed 真正执行查询以及后续补数时，数据源上下文已就绪。
 */
@Slf4j
@Aspect
@Component
@Order(0)
public class EtlTriggerAspect {

    private final EtlClient etlClient;

    @Autowired
    public EtlTriggerAspect(EtlClient etlClient) {
        this.etlClient = etlClient;
    }

    /**
     * 环绕通知：先执行原始查询，结果为空时触发 ETL 补数。
     *
     * @param pjp 连接点
     * @return 原始查询结果（空结果也会原样返回）
     * @throws Throwable 查询本身抛出的异常照常抛出，不吞掉
     */
    @Around("@annotation(com.reports.annotation.EtlTask)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        EtlTask et = method.getAnnotation(EtlTask.class);

        // 先执行原始查询。查询本身抛出的异常属于数据库/业务问题，不属于补数职责，照常抛出、不吞掉。
        Object result = pjp.proceed();

        // 结果非空：直接返回，不触发补数。
        if (!isEmptyResult(result)) {
            return result;
        }

        // 结果为空：进入「尽力补数」。补数过程中的任何问题都不得影响本次查询返回，因此整体 try-catch 兜住。
        log.info("检测到空结果，尝试触发 ETL 补数: taskToken={}", et.taskToken());
        try {
            String ds = DynamicDataSourceContextHolder.get();
            String traceId = MDC.get("traceId");
            if (hasRangeExpansion(et)) {
                triggerByDay(pjp, et, ds, traceId);
            } else {
                Map<String, Object> vars = extractVars(method, pjp.getArgs(), et.params(), et.dateFormats());
                log.info("ETL 补数触发请求体: taskToken={}, vars={}", et.taskToken(), vars);
                etlClient.ensure(ds, et.taskToken(), traceId, vars);
            }
        } catch (Throwable ex) {
            log.error("ETL 补数触发异常（不影响本次查询返回）: taskToken={}", et.taskToken(), ex);
        }

        // 返回原始（空）结果，本次请求仍按空结果返回。
        return result;
    }

    /**
     * 是否启用「范围按天展开」补数。
     */
    private boolean hasRangeExpansion(EtlTask et) {
        return et.rangeStart() != null && !et.rangeStart().isEmpty()
                && et.rangeEnd() != null && !et.rangeEnd().isEmpty()
                && et.rangeTarget() != null && !et.rangeTarget().isEmpty();
    }

    /**
     * 范围展开补数：把 [rangeStart, rangeEnd] 逐日拆分，每天以 {@code {rangeTarget: 当日}} 触发一次。
     * <p>
     * 起点/终点参数缺失或不是 Date 时退化为整段一次触发（vars 仅含 rangeTarget=起点，避免空 vars 误补）。
     * 超出 rangeMaxDays 按最大天数截断，防止超大范围刷爆触发线程池。
     * 去重由 {@link com.reports.service.EtlClient} 按 (taskToken + 参数) 按天自然生效。
     */
    private void triggerByDay(ProceedingJoinPoint pjp, EtlTask et, String ds, String traceId) {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        Map<String, Object> rangeArgs = extractVars(method, pjp.getArgs(),
                new String[]{et.rangeStart(), et.rangeEnd()}, new String[0]);
        Object startObj = rangeArgs.get(et.rangeStart());
        Object endObj = rangeArgs.get(et.rangeEnd());

        java.time.format.DateTimeFormatter parseFormatter =
                java.time.format.DateTimeFormatter.ofPattern(et.rangeParsePattern());
        java.time.LocalDate startDay = resolveDay(startObj, parseFormatter, true);
        java.time.LocalDate endDay = resolveDay(endObj, parseFormatter, false);
        if (startDay == null || endDay == null) {
            log.warn("范围展开参数缺失或无法解析，退化为整段触发: taskToken={}, rangeStart={}, rangeEnd={}",
                    et.taskToken(), et.rangeStart(), et.rangeEnd());
            Map<String, Object> vars = new LinkedHashMap<>();
            if (startObj != null) {
                vars.put(et.rangeTarget(), startObj);
            }
            etlClient.ensure(ds, et.taskToken(), traceId, vars);
            return;
        }
        if (endDay.isBefore(startDay)) {
            log.warn("范围展开起点晚于终点，跳过补数: taskToken={}, start={}, end={}",
                    et.taskToken(), startDay, endDay);
            return;
        }

        long days = java.time.temporal.ChronoUnit.DAYS.between(startDay, endDay) + 1;
        if (days > et.rangeMaxDays()) {
            log.warn("范围展开天数 {} 超过上限 {}，按上限截断: taskToken={}", days, et.rangeMaxDays(), et.taskToken());
            endDay = startDay.plusDays(et.rangeMaxDays() - 1L);
        }

        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.ofPattern(et.rangePattern());
        for (java.time.LocalDate d = startDay; !d.isAfter(endDay); d = d.plusDays(1)) {
            Map<String, Object> vars = new LinkedHashMap<>();
            vars.put(et.rangeTarget(), d.format(formatter));
            etlClient.ensure(ds, et.taskToken(), traceId, vars);
        }
    }

    /**
     * 把范围端点实参解析成 LocalDate。
     * <p>
     * Date 直接取日期部分；String 先按 LocalDate 解析，失败按 YearMonth / Year 兜底
     * （起点取月初/年初，终点取月末/年末，保证整个月/年被展开覆盖）。解析不了返回 null。
     *
     * @param arg       实参（Date 或 String）
     * @param formatter 解析格式（来自 rangeParsePattern）
     * @param isStart   true=起点（取月初/年初），false=终点（取月末/年末）
     */
    private java.time.LocalDate resolveDay(Object arg,
                                           java.time.format.DateTimeFormatter formatter,
                                           boolean isStart) {
        if (arg == null) {
            return null;
        }
        if (arg instanceof Date) {
            return toLocalDate((Date) arg);
        }
        if (!(arg instanceof String)) {
            return null;
        }
        String text = ((String) arg).trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return java.time.LocalDate.parse(text, formatter);
        } catch (java.time.format.DateTimeParseException ignored) {
            // 继续按 YearMonth / Year 兜底
        }
        try {
            java.time.YearMonth ym = java.time.YearMonth.parse(text, formatter);
            return isStart ? ym.atDay(1) : ym.atEndOfMonth();
        } catch (java.time.format.DateTimeParseException ignored) {
            // 继续按 Year 兜底
        }
        try {
            java.time.Year year = java.time.Year.parse(text, formatter);
            java.time.LocalDate first = year.atDay(1);
            return isStart ? first : year.atMonth(12).atEndOfMonth();
        } catch (java.time.format.DateTimeParseException e) {
            return null;
        }
    }

    private java.time.LocalDate toLocalDate(Date date) {
        long epochMilli = date.getTime();
        long zoneOffset = java.util.TimeZone.getDefault().getOffset(epochMilli);
        long epochDay = (epochMilli + zoneOffset) / (1000L * 60 * 60 * 24);
        return java.time.LocalDate.ofEpochDay(epochDay);
    }

    /**
     * 判断查询结果是否为「空」。
     * <p>
     * 仅对以下类型判空，命中才返回 true（触发补数）：
     * <ol>
     *     <li>Collection：isEmpty()</li>
     *     <li>Map：isEmpty()</li>
     *     <li>Optional：empty（无值）</li>
     *     <li>数组：length == 0（含基本类型数组，故用反射取长度）</li>
     * </ol>
     * 其余情况（含 null、普通 POJO 等）一律返回 false —— 只有集合 / Map / Optional / 数组判空。
     *
     * @param result 查询结果
     * @return true 表示空结果，需要触发补数
     */
    private boolean isEmptyResult(Object result) {
        if (result instanceof Collection) {
            return ((Collection<?>) result).isEmpty();
        }
        if (result instanceof Map) {
            return ((Map<?, ?>) result).isEmpty();
        }
        if (result instanceof Optional) {
            return !((Optional<?>) result).isPresent();
        }
        if (result != null && result.getClass().isArray()) {
            return Array.getLength(result) == 0;
        }
        return false;
    }

    /**
     * 抽取 ETL 补数所需的变量 Map。
     * <p>
     * 规则：
     * <ol>
     *     <li>以方法参数上的 MyBatis {@link Param} 注解值作为变量名，参数没有 {@link Param} 注解则跳过；</li>
     *     <li>若白名单（et.params()）非空，则仅保留白名单内的参数名；</li>
     *     <li>实参为 null 的跳过，避免把 null 传给 ETL；</li>
     *     <li>使用 LinkedHashMap 保持参数声明顺序。</li>
     * </ol>
     *
     * @param method    被拦截的方法
     * @param args      被拦截方法的实参
     * @param whitelist  注解上声明的参数名白名单（可空数组表示不过滤）
     * @param dateFormats 注解上声明的日期格式声明（形如 "参数名=yyyy-MM-dd"，可空数组表示不转换）
     * @return 参数名到实参值的映射，可能为空 Map
     */
    private Map<String, Object> extractVars(Method method, Object[] args, String[] whitelist, String[] dateFormats) {
        Map<String, Object> vars = new LinkedHashMap<>();
        if (args == null) {
            return vars;
        }

        // 解析 dateFormats："参数名=pattern" → 参数名 -> 日期格式
        Map<String, String> formats = new LinkedHashMap<>();
        if (dateFormats != null) {
            for (String df : dateFormats) {
                if (df == null) {
                    continue;
                }
                int idx = df.indexOf('=');
                if (idx > 0 && idx < df.length() - 1) {
                    formats.put(df.substring(0, idx).trim(), df.substring(idx + 1).trim());
                } else {
                    log.warn("dateFormats 配置格式应为 '参数名=yyyy-MM-dd'，忽略: [{}]", df);
                }
            }
        }

        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            // 防御：实参与形参个数不一致时不再继续，避免数组越界。
            if (i >= args.length) {
                break;
            }
            Param param = parameters[i].getAnnotation(Param.class);
            if (param == null) {
                // 没有 @Param 注解，无法确定变量名，跳过。
                continue;
            }
            String name = param.value();
            // 白名单非空且该参数名不在白名单内，跳过。
            if (whitelist != null && whitelist.length > 0 && !Arrays.asList(whitelist).contains(name)) {
                continue;
            }
            // 实参为 null，跳过。
            if (args[i] == null) {
                continue;
            }
            Object value = args[i];
            // dateFormats 命中且实参为 Date 时，按指定格式转成字符串再透传
            String pattern = formats.get(name);
            if (pattern != null && value instanceof Date) {
                try {
                    value = new SimpleDateFormat(pattern).format((Date) value);
                } catch (IllegalArgumentException e) {
                    log.warn("dateFormats 日期格式非法，按原值透传: 参数=[{}], pattern=[{}]", name, pattern);
                }
            }
            vars.put(name, value);
        }
        return vars;
    }

}
