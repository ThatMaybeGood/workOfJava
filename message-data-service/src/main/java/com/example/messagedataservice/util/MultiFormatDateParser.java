package com.example.messagedataservice.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

public class MultiFormatDateParser {

    // 定义所有支持的日期格式
    private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
            // 1. 标准 ISO 格式 (默认支持，但显式列出有助于理解)
            DateTimeFormatter.ISO_LOCAL_DATE, // YYYY-MM-DD
            // 2. DD/MM/YYYY 格式
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            // 3. YYYYMMDD 格式 (无分隔符)
            DateTimeFormatter.ofPattern("yyyyMMdd"),
            // 4. MM-dd-yyyy 格式
            DateTimeFormatter.ofPattern("MM-dd-yyyy")
            // 您可以在这里添加更多需要的格式，例如 "dd-MM-yyyy", "yyyy.MM.dd" 等
    );

    /**
     * 尝试使用预定义的多种格式将字符串解析为 LocalDate。
     * * @param dateString 要解析的日期字符串。
     * @return 转换成功的 LocalDate 对象。
     * @throws DateTimeParseException 如果所有格式都解析失败。
     */
    public static LocalDate parseMultiFormatDate(String dateString) {
        // 遍历所有预定义的格式化器
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                // 尝试用当前格式解析字符串
                return LocalDate.parse(dateString, formatter);
            } catch (DateTimeParseException ignored) {
                // 如果解析失败，忽略异常，继续尝试下一个格式
                continue;
            }
        }

        // 如果循环结束，说明所有格式都解析失败，抛出异常
        throw new DateTimeParseException(
                "无法使用任何已知格式解析日期字符串: " + dateString,
                dateString,
                0
        );
    }

    public static void main(String[] args) {
        // 示例测试
        String date1 = "2025-11-25"; // ISO
        String date2 = "25/11/2025"; // DD/MM/YYYY
        String date3 = "20251125";   // YYYYMMDD
        String date4 = "11-25-2025"; // MM-dd-yyyy
        String date5 = "2025.11.25"; // 错误的格式

        try {
            System.out.println(date1 + " -> " + parseMultiFormatDate(date1));
            System.out.println(date2 + " -> " + parseMultiFormatDate(date2));
            System.out.println(date3 + " -> " + parseMultiFormatDate(date3));
            System.out.println(date4 + " -> " + parseMultiFormatDate(date4));

            // 尝试解析一个不支持的格式，会抛出异常
            System.out.println(date5 + " -> " + parseMultiFormatDate(date5));

        } catch (DateTimeParseException e) {
            System.err.println("\n[错误] " + e.getMessage());
        }
    }
}