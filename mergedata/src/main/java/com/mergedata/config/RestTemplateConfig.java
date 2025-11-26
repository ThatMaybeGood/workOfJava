package com.mergedata.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
//        return new RestTemplate();
        // 使用 SimpleClientHttpRequestFactory 来设置超时
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // 设置连接超时：建立连接的最长时间（例如 3 秒）
        factory.setConnectTimeout(3000);

        // 设置读取超时：等待数据传输的最长时间（例如 5 秒）
        factory.setReadTimeout(5000);

        // 返回带有自定义配置的 RestTemplate
        return new RestTemplate(factory);
    }
}
