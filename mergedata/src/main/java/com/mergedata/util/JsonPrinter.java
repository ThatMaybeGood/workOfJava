package com.mergedata.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * 极简 JSON 打印工具
 * 只做一件事：把对象漂亮地打印出来
 */
public final class JsonPrinter {

    private static final ObjectMapper PRETTY_MAPPER = new ObjectMapper();

    static {
        PRETTY_MAPPER.registerModule(new JavaTimeModule());
        PRETTY_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        PRETTY_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
    }

    private JsonPrinter() {
        // 工具类，防止实例化
    }

    /**
     * 漂亮的打印任何对象
     */
    public static void prettyPrint(Object obj) {
        try {
            System.out.println(PRETTY_MAPPER.writeValueAsString(obj));
        } catch (JsonProcessingException e) {
            System.err.println("⚠️ JSON 打印失败: " + e.getMessage());
            System.out.println(obj); // 降级：使用默认 toString
        }
    }

    /**
     * 打印并返回 JSON 字符串
     */
    public static String printAndReturn(Object obj) {
        try {
            String json = PRETTY_MAPPER.writeValueAsString(obj);
            System.out.println(json);
            return json;
        } catch (JsonProcessingException e) {
            String error = "JSON转换失败: " + e.getMessage();
            System.err.println(error);
            return error;
        }
    }

    /**
     * 带标题的打印
     */
    public static void printWithTitle(String title, Object obj) {
        System.out.println("\n════════════ " + title + " ════════════");
        prettyPrint(obj);
        System.out.println("═════════════════════════════════════════\n");
    }

    /**
     * 打印列表（自动处理空值和数量）
     */
    public static <T> void printList(String title, java.util.List<T> list) {
        System.out.println("\n📋 " + title);
        if (list == null) {
            System.out.println("  列表为 null");
        } else if (list.isEmpty()) {
            System.out.println("  列表为空 (0 条记录)");
        } else {
            System.out.println("  共 " + list.size() + " 条记录");
            System.out.println("  ──────────────────");
            for (int i = 0; i < Math.min(list.size(), 3); i++) {
                System.out.print("  [" + (i + 1) + "] ");
                prettyPrint(list.get(i));
            }
            if (list.size() > 3) {
                System.out.println("  ... 还有 " + (list.size() - 3) + " 条未显示");
            }
        }
        System.out.println();
    }
}