package com.mergedata.config;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // <-- 保留
import com.mergedata.util.JsonStringToBigDecimalDeserializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder -> {

            // 1. 设置驼峰转下划线命名策略
            builder.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

            // 2. 统一注册所有模块
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            SimpleModule customModule = new SimpleModule();

            // 注册 BigDecimal Deserializer
            customModule.addDeserializer(BigDecimal.class, new JsonStringToBigDecimalDeserializer());

            // 合并所有模块注册，避免多次调用 builder.modules() 可能带来的不确定性
            builder.modules(javaTimeModule, customModule);

            // 注意：这里不需要手动指定日期格式，因为我们依赖 DTO 上的 @JsonFormat。
        };
    }
}