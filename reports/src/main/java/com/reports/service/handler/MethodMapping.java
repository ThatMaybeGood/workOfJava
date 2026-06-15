package com.reports.service.handler;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 方法路由注解 - 标注在 ReportHandler 实现类上，声明该处理器处理的 method 值。
 * 使用此注解后无需再实现 getMethod() 方法，Factory 会自动从注解中读取路由键。
 *
 * 示例：
 * <pre>
 *   &#64;MethodMapping("reports.outp.outpatient-operation")
 *   &#64;Component
 *   public class OutpatientOperationHandler implements ReportHandler&lt;...&gt; { ... }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MethodMapping {

    /**
     * 路由键，对应请求中的 method 字段
     */
    String value();

}
