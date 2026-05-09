package com.demo.integration.core.executor;

import com.alibaba.fastjson.JSON;
import com.demo.integration.config.IntegrationProperties;
import com.demo.integration.core.adapter.PostAdapter;
import com.demo.integration.core.retry.RetryExecutor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2026/5/9 23:11
 */

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

@Slf4j
@Component
public class PostExecutor {

    @Resource
    private RestTemplate restTemplate;

    /**
     * POST请求
     */
    public String execute(String url, Object body) {

        try {

            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity =
                    new HttpEntity<>(JSON.toJSONString(body), headers);

            log.info("开始请求下游地址 url={} body={}", url, body);

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            entity,
                            String.class
                    );

            log.info("下游返回 response={}", response.getBody());

            return response.getBody();

        } catch (Exception e) {

            log.error("POST请求异常", e);

            throw new RuntimeException("POST请求失败");
        }
    }
}