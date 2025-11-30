package com.mergedata.util;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 业务主键生成工具类 (包含日期信息)。
 * 优化后格式: YYYYMMddHH + 4位随机码，总长 14 位。
 */
public class PrimaryKeyGenerator {

    // 格式化器: 年月日时 (10位)
    // 从 YYYYMMddHHmmss (14位) 缩短到 YYYYMMddHH (10位)
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHH");

    /**
     * 生成包含日期和随机数的唯一主键。
     *
     * @return 长度为 14 位的字符串主键，如 20251130161234
     */
    public static String generateKey() {
        // 1. 获取当前时间并格式化为 YYYYMMddHH (10位)
        String dateTimePart = LocalDateTime.now().format(DATE_TIME_FORMATTER);

        // 2. 生成 4 位随机码 (避免同一小时内冲突)
        // 使用 ThreadLocalRandom 提高并发性能
        // 生成 1000 到 9999 之间的随机数
        int randomPart = ThreadLocalRandom.current().nextInt(1000, 10000);

        // 3. 拼接并返回主键
        // 主键总长: 10位 (时间) + 4位 (随机) = 14位
        return dateTimePart + randomPart;
    }

    /**
     * 生成包含日期和序列号的唯一主键（如果使用外部序列生成器）。
     *
     * @param sequence 外部生成的序列号 (例如, 数据库序列或分布式 ID)
     * @return 包含日期前缀的主键 (总长约 14 位)
     */
    public static String generateKeyWithSequence(long sequence) {
        String dateTimePart = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        // 截取序列号的后几位作为后缀，确保长度可控
        String seqSuffix = String.format("%04d", sequence % 10000);
        // 主键总长: 10位 (时间) + 4位 (序列) = 14位
        return dateTimePart + seqSuffix;
    }

    /*
    public static void main(String[] args) {
        System.out.println("生成主键: " + generateKey());
        // 预期输出类似: 20251130168759
    }
    */
}