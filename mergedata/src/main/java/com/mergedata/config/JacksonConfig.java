package com.mergedata.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.mergedata.util.JsonStringToBigDecimalDeserializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

@Configuration
public class JacksonConfig {

    /** 统一日期时间格式 */
    private static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    /** 统一日期格式 */
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder -> {

            // 允许 JSON 数字前导零
            builder.featuresToEnable(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS);

            // 允许解析单引号
            builder.featuresToEnable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
            // 允许解析非引号的字段名
            builder.featuresToEnable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);

            // 设置驼峰转下划线命名策略 (如: createTime -> create_time)
            builder.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

            // 配置 Java 8 时间模块
            JavaTimeModule javaTimeModule = new JavaTimeModule();

            // 序列化 LocalDateTime（统一输出为 yyyy-MM-dd HH:mm:ss）
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
            javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));

            // 使用自定义的反序列化器，支持多种输入格式
            javaTimeModule.addDeserializer(LocalDateTime.class, new MultiFormatLocalDateTimeDeserializer());

            // 序列化与反序列化 LocalDate
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
            javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
            javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));

            // 配置自定义业务模块 (如 BigDecimal 处理)
            SimpleModule customModule = new SimpleModule();
            customModule.addDeserializer(BigDecimal.class, new JsonStringToBigDecimalDeserializer());

            // 统一注册模块
            builder.modules(javaTimeModule, customModule);

            // 禁用“日期转时间戳”功能，确保输出的是字符串而不是一串数字
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        };
    }

    /**
     * 支持多种格式的 LocalDateTime 反序列化器
     * 支持格式：
     * 1. yyyy-MM-dd HH:mm:ss
     * 2. yyyy-MM-dd
     * 3. yyyy-MM-dd'T'HH:mm:ss (ISO格式)
     * 4. yyyy-MM-dd'T'HH:mm:ss HH:mm:ss (错误格式，包含重复时间)
     */
    private static class MultiFormatLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

        private static final List<String> SUPPORTED_FORMATS = Arrays.asList(
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss HH:mm:ss"
        );

        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String dateStr = p.getText();
            if (dateStr == null || dateStr.trim().isEmpty()) {
                return null;
            }

            dateStr = dateStr.trim();

            // 遍历所有支持的格式进行解析
            for (String format : SUPPORTED_FORMATS) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                    return LocalDateTime.parse(dateStr, formatter);
                } catch (DateTimeParseException e) {
                    // 继续尝试下一个格式
                }
            }

            // 所有格式都失败，抛出异常
            throw new IOException("无法解析日期时间格式: " + dateStr +
                    "，支持的格式: " + String.join(", ", SUPPORTED_FORMATS));
        }
    }
}