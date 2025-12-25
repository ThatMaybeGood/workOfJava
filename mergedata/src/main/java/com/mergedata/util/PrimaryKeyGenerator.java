package com.mergedata.util;


import com.baomidou.mybatisplus.core.toolkit.IdWorker;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        //  MyBatis-Plus，直接调用它内置的雪花算法
        // IdWorker.getIdStr() 会返回一个 19 位的分布式唯一 ID 字符串
        return IdWorker.getIdStr();    }

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

}