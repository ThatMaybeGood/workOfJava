package com.mergedata.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class RequestLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        // 1. 打印请求报文
        logRequest(request, body);

        // 2. 执行请求
        ClientHttpResponse response = execution.execute(request, body);

        // 3. 打印响应报文 (可选，但推荐)
        logResponse(response);

        return response;
    }

    // 打印请求信息的私有方法
    private void logRequest(HttpRequest request, byte[] body) {
        if (log.isDebugEnabled()) {
            log.debug("===================== REQUEST START =====================");
            log.debug("URI         : {}", request.getURI());
            log.debug("Method      : {}", request.getMethod());
            log.debug("Headers     : {}", request.getHeaders());
            // 打印请求体（假设是 UTF-8 编码）
            log.debug("Request Body: {}", new String(body, StandardCharsets.UTF_8));
            log.debug("====================== REQUEST END ======================");
        }
    }

    // 打印响应信息的私有方法
    private void logResponse(ClientHttpResponse response) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("==================== RESPONSE START =====================");
            log.debug("Status Code : {}", response.getStatusCode());
            log.debug("Status Text : {}", response.getStatusText());
            log.debug("Headers     : {}", response.getHeaders());
            // 响应体是流，只能读取一次。这里先读到String，再重新包装成流返回。
            String responseBody = StreamUtils.copyToString(response.getBody(), Charset.defaultCharset());
            log.debug("Response Body: {}", responseBody);
            log.debug("===================== RESPONSE END ======================");
        }
    }
}