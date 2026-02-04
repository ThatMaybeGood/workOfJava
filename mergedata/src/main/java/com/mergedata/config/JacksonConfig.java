package com.mergedata.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.mergedata.util.JsonStringToBigDecimalDeserializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.fasterxml.jackson.core.JsonParser;

@Configuration
public class JacksonConfig {

    /** 统一日期时间格式 */
    private static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    /** 统一日期格式 */
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder -> {

            // 方案 A: 允许 JSON 数字前导零
            builder.featuresToEnable(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS);

            // 允许解析单引号
            builder.featuresToEnable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
            // 允许解析非引号的字段名
            builder.featuresToEnable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);

            // 1. 设置驼峰转下划线命名策略 (如: createTime -> create_time)
            builder.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

            // 2. 配置 Java 8 时间模块 (处理 LocalDateTime 的 T 问题)
            JavaTimeModule javaTimeModule = new JavaTimeModule();

            // 序列化与反序列化 LocalDateTime
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
            javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
            javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));

            // 序列化与反序列化 LocalDate
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
            javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
            javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));

            // 3. 配置自定义业务模块 (如 BigDecimal 处理)
            SimpleModule customModule = new SimpleModule();
            customModule.addDeserializer(BigDecimal.class, new JsonStringToBigDecimalDeserializer());

            // 4. 统一注册模块
            // 注意：JavaTimeModule 必须显式注册，否则它会使用默认的 ISO-8601 格式输出 T
            builder.modules(javaTimeModule, customModule);

            // 5. 禁用“日期转时间戳”功能，确保输出的是字符串而不是一串数字
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        };
    }
}