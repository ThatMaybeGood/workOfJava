package com.mergedata.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {

        // --- 1. 超时设置（保持不变） ---
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);

        // --- 2. 创建 RestTemplate 实例 ---
        RestTemplate restTemplate = new RestTemplate(factory);

        // --- 3. 强制 JSON 转换器处理 application/xml ---

        // 获取并修改默认的 HttpMessageConverter 列表
        List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();

        // 遍历找到默认的 JSON 转换器（MappingJackson2HttpMessageConverter）
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                MappingJackson2HttpMessageConverter jsonConverter =
                        (MappingJackson2HttpMessageConverter) converter;

                // 获取当前支持的 MediaTypes 列表（需要创建新的可修改列表）
                List<MediaType> supportedMediaTypes =
                        new ArrayList<>(jsonConverter.getSupportedMediaTypes());

                // 关键步骤：将 application/xml 添加到 JSON 转换器支持的类型列表中
                // 这样，当 RestTemplate 收到 application/xml 响应时，它会交给 Jackson JSON 转换器处理
                if (!supportedMediaTypes.contains(MediaType.APPLICATION_XML)) {
                    supportedMediaTypes.add(MediaType.APPLICATION_XML);
                    supportedMediaTypes.add(new MediaType("application", "xml")); // 额外的保险
                }

                // 更新支持列表
                jsonConverter.setSupportedMediaTypes(supportedMediaTypes);

                // 找到并修改完成后，跳出循环
                break;
            }
        }

        // --- 4. 返回配置完成的 RestTemplate ---
        return restTemplate;
    }
}