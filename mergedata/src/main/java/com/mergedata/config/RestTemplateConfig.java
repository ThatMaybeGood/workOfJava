package com.mergedata.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Configuration
public class RestTemplateConfig {

    // 关键改变 1: 声明并注入我们自定义配置的 ObjectMapper
    // 这个 ObjectMapper 会自动包含 JacksonConfig 中设置的 SNAKE_CASE 命名策略
    private final ObjectMapper objectMapper;

    @Autowired // 通过构造器注入 JacksonConfig 中定义的 ObjectMapper Bean
    public RestTemplateConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public RestTemplate restTemplate() {

        // --- 1. 超时设置 ---
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);

        // ！！！【关键修复】：用 BufferingClientHttpRequestFactory 包装原始工厂 ！！！
        // 解决响应流只能读取一次的问题（让拦截器和转换器都能读取）
        ClientHttpRequestFactory bufferedFactory = new BufferingClientHttpRequestFactory(factory);

        // --- 2. 创建 RestTemplate 实例 ---
        RestTemplate restTemplate = new RestTemplate(bufferedFactory);

        // --- 3. 添加请求拦截器 (日志功能) ---
        // 确保 RequestLoggingInterceptor.java 也是最新的简洁版本
        restTemplate.setInterceptors(Collections.singletonList(new RequestLoggingInterceptor()));

        // --- 4. 配置 HttpMessageConverter 列表 ---
        List<HttpMessageConverter<?>> converters = new ArrayList<>();

        // 4.1 **优先添加 JSON 转换器 (使用注入的 ObjectMapper，解决了命名策略和流读取问题)**
        MappingJackson2HttpMessageConverter jsonConverter =
                new MappingJackson2HttpMessageConverter(this.objectMapper);

        // 配置 JSON 转换器支持的 MediaType
        List<MediaType> supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        supportedMediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
        supportedMediaTypes.add(MediaType.APPLICATION_XML); // 保持对 XML 的支持
        supportedMediaTypes.add(new MediaType("application", "xml", StandardCharsets.UTF_8));

        // 添加这一行，让 Jackson 转换器也负责处理被标为 text/html 的数据
        supportedMediaTypes.add(MediaType.TEXT_HTML);

        jsonConverter.setSupportedMediaTypes(supportedMediaTypes);
        converters.add(jsonConverter); // 【优先添加 JSON 转换器】


        // 4.2 **随后添加 String 转换器**
        // 解决中文乱码问题：强制 String 转换器使用 UTF-8
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        converters.add(stringConverter); // 【随后添加 String 转换器】


        // 4.3 设置自定义的 Converters 列表
        restTemplate.setMessageConverters(converters);

        // --- 5. 返回配置完成的 RestTemplate ---
        return restTemplate;
    }
}