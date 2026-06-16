package com.reports.annotation;

import java.lang.annotation.*;

/**
 * 数据源切换注解
 * <p>
 * 标注在方法或类上，声明该方法/类使用指定的数据源。
 * 配合 AOP 切面 {@link com.reports.aspect.DataSourceAspect} 实现自动切换。
 *
 * 示例：
 * <pre>
 *   &#64;DataSource("slave")
 *   public List&lt;User&gt; queryFromSlave() { ... }
 * </pre>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataSource {

    /**
     * 数据源名称，对应 DataSourceConfig 中配置的数据源 key
     * 默认使用 master
     */
    String value() default "master";

}
