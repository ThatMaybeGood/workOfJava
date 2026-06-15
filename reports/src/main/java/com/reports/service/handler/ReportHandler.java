package com.reports.service.handler;

import com.reports.dto.common.ApiRequest;
import com.reports.dto.common.ApiResponse;

/**
 * 报表处理器接口
 * <p>
 * 实现类需使用 {@link MethodMapping} 注解声明路由键，例如：
 * <pre>
 *   &#64;MethodMapping("reports.outp.outpatient-operation")
 *   &#64;Component
 *   public class XxxHandler implements ReportHandler&lt;XxxRequest, XxxResponse&gt; { ... }
 * </pre>
 *
 * @param <T> 请求 Body 类型
 * @param <R> 响应 Body 类型
 */
public interface ReportHandler<T, R> {

    /**
     * 处理请求
     * @param request 统一请求对象（body 可能是 LinkedHashMap，需要手动转换）
     */
    ApiResponse<R> handle(ApiRequest<Object> request);

}
