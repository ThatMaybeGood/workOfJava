package com.mergedata.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class HttpUtils {

    @Autowired
    private RestTemplate restTemplate;

    // 通用 GET 请求
    public <T> T get(String url, Class<T> responseType, Object... uriVariables) {
        try {
            ResponseEntity<T> response = restTemplate.getForEntity(url, responseType, uriVariables);
            return handleResponse(response);
        } catch (Exception e) {
            log.error("GET request failed: {}", e.getMessage());
            throw new RuntimeException("HTTP request failed", e);
        }
    }

    // 通用 POST 请求
    public <T, R> T post(String url, R requestBody, Class<T> responseType) {
        try {
            ResponseEntity<T> response = restTemplate.postForEntity(url, requestBody, responseType);
            return handleResponse(response);
        } catch (Exception e) {
            log.error("POST request failed: {}", e.getMessage());
            throw new RuntimeException("HTTP request failed", e);
        }
    }

    // 处理响应
    private <T> T handleResponse(ResponseEntity<T> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            log.error("HTTP request failed with status: {}", response.getStatusCode());
            throw new RuntimeException("HTTP request failed with status: " + response.getStatusCode());
        }
    }

    // 带超时设置的请求
    public <T> T getWithTimeout(String url, Class<T> responseType, int timeoutSeconds) {
        RestTemplate timeoutTemplate = new RestTemplate(getClientHttpRequestFactory(timeoutSeconds));
        return timeoutTemplate.getForObject(url, responseType);
    }

    private ClientHttpRequestFactory getClientHttpRequestFactory(int timeout) {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(timeout * 1000);
        clientHttpRequestFactory.setReadTimeout(timeout * 1000);
        return clientHttpRequestFactory;
    }
}
