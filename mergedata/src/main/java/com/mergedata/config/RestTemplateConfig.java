package com.mergedata.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {

        // --- 1. 超时设置 ---
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);

        // --- 2. 创建 RestTemplate 实例 ---
        RestTemplate restTemplate = new RestTemplate(factory);




        // ========================================================
        // === 关键修改：添加请求拦截器来打印请求报文 ===
        // ========================================================
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new RequestLoggingInterceptor());
        restTemplate.setInterceptors(interceptors);
        // ========================================================




        // 获取可修改的 HttpMessageConverter 列表
        List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();

        // --- 3. 解决中文乱码问题：强制 String 转换器使用 UTF-8 ---
        // 查找并移除默认的 StringHttpMessageConverter
        converters.removeIf(converter -> converter instanceof StringHttpMessageConverter);

        // 创建一个新的 StringHttpMessageConverter，并强制使用 UTF-8
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);

        // 将新的 UTF-8 String 转换器添加到列表的首位（优先级最高）
        converters.add(0, stringConverter);


        // --- 4. 强制 JSON 转换器处理 application/xml ---

        // 遍历找到默认的 JSON 转换器（MappingJackson2HttpMessageConverter）
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                MappingJackson2HttpMessageConverter jsonConverter =
                        (MappingJackson2HttpMessageConverter) converter;

                // 获取当前支持的 MediaTypes 列表
                List<MediaType> supportedMediaTypes =
                        new ArrayList<>(jsonConverter.getSupportedMediaTypes());

                // 关键步骤：将 application/xml 添加到 JSON 转换器支持的类型列表中
                if (!supportedMediaTypes.contains(MediaType.APPLICATION_XML)) {
                    supportedMediaTypes.add(MediaType.APPLICATION_XML);
                    // 确保 Media Type 的定义是正确的，避免字符集问题
                    supportedMediaTypes.add(new MediaType("application", "xml", StandardCharsets.UTF_8));
                }

                // 更新支持列表
                jsonConverter.setSupportedMediaTypes(supportedMediaTypes);

                break;
            }
        }

        // --- 5. 返回配置完成的 RestTemplate ---
        return restTemplate;
    }
}