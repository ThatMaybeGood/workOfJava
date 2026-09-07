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
            Map<String, Object> vars = extractVars(method, pjp.getArgs(), et.params(), et.dateFormats());
            String ds = DynamicDataSourceContextHolder.get();
            etlClient.ensure(ds, et.taskToken(), vars);
        } catch (Throwable ex) {
            log.error("ETL 补数触发异常（不影响本次查询返回）: taskToken={}", et.taskToken(), ex);
        }

        // 返回原始（空）结果，本次请求仍按空结果返回。
        return result;
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
