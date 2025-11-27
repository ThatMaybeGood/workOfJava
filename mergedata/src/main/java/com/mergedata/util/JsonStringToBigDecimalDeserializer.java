package com.mergedata.util;// File: com.mergedata.util.com.mergedata.util.JsonStringToBigDecimalDeserializer.java (或您放置的位置)

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.math.BigDecimal;

// 继承自 JsonDeserializer<BigDecimal> 或 StdDeserializer<BigDecimal> 均可
public class JsonStringToBigDecimalDeserializer extends JsonDeserializer<BigDecimal> {

    // 如果继承 StdDeserializer，需要无参构造
    // public com.mergedata.util.JsonStringToBigDecimalDeserializer() { super(BigDecimal.class); }

    @Override
    public BigDecimal deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        // p.getText() 可以处理带引号的 JSON 字符串
        String value = p.getText();

        // 关键处理：处理 null 或空字符串
        if (value == null || value.trim().isEmpty() || "null".equalsIgnoreCase(value.trim())) {
            // 确保返回 BigDecimal.ZERO 而不是 null，避免下游出现 NullPointerException
            return BigDecimal.ZERO;
        }

        try {
            // 注意：trim() 可能会去除 JSON 字符串中的引号，但 Jackson 已经帮您处理了引号，这里主要是去除可能的空格。
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            // 增加调试信息，检查是否有非数字内容导致失败
            System.err.println("Jackson Deserializer Error: Failed to convert JSON String '" + value + "' to BigDecimal. Returning ZERO.");
            // 失败时返回 0 或根据业务需求抛出异常
            return BigDecimal.ZERO;
        }
    }
}