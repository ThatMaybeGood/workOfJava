package com.showexcel.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ApiRootController {
    // Jackson 的核心工具类
    private final ObjectMapper objectMapper = new ObjectMapper();
    @GetMapping("")
    public String apiRoot() {
        return "API endpoints available: \n" +
               "- /api/cash-statistics - 现金统计相关API\n" +
               "- /api/holidays - 节假日管理API";
    }

    @GetMapping("/")
    public String apiRootSlash() {
        return apiRoot();
    }
    /**
     * 方法一：使用 @RequestBody String
     * 适用于大多数文本内容类型 (application/json, text/plain, application/xml 等)
     */
    @PostMapping("/all")
    public String receiveGenericBody(@RequestBody String requestBody) {
        System.out.println("--- 接收到的原始字符串内容 ---");
        System.out.println(requestBody);
        System.out.println("--------------------------------");

        try {
        // 1. 反序列化：将原始 JSON 字符串解析为通用的 JsonNode
        JsonNode jsonNode = objectMapper.readTree(requestBody);

        System.out.println("--- 反序列化为 JsonNode 后的打印 ---");

        // 2. 遍历并打印 JSON 节点的键值对（只适用于对象，不适用于数组）
        if (jsonNode.isObject()) {
            jsonNode.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode value = entry.getValue();

                // 打印键和值，使用 .toPrettyString() 格式化嵌套对象/数组
                System.out.printf("Key: %s, Value: %s%n", key, value.toPrettyString().trim());
            });
        } else if (jsonNode.isArray()) {
            System.out.println("内容是一个 JSON 数组，包含 " + jsonNode.size() + " 个元素。");
            // 如果需要，可以进一步遍历数组元素
            for (int i = 0; i < jsonNode.size(); i++) {
                System.out.println("Element " + i + ": " + jsonNode.get(i).toString());
            }
        } else {
            System.out.println("内容是原始值 (非对象/数组): " + jsonNode.asText());
        }


        return "JSON 内容已接收、解析并打印到控制台。";

    } catch (Exception e) {
        // 如果传入的 String 不是有效的 JSON 格式，将会抛出异常
        System.err.println("错误：接收到的字符串不是有效的 JSON 格式。");
        e.printStackTrace();
        // 返回 400 错误状态码会更规范，但这里我们先返回字符串
        return "错误：内容解析失败，它不是一个有效的 JSON 字符串。";
    }
}
 }