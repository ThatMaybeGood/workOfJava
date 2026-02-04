package com.mergedata.util;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2026/2/4 21:29
 */

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class OracleBatchUtil {

    /**
     * 批量写入数据到 Oracle 数据库
     * @param jdbcTemplate JdbcTemplate 实例
     * @param data 要写入的数据列表
     * @param tableName 目标表名
     * @param batchSize 批量大小
     * @param isPrintParameters 是否打印参数
     * @param <T> 数据类型
     */
    public static <T> void fastBatchInsert(JdbcTemplate jdbcTemplate, List<T> data, String tableName, int batchSize,boolean isPrintParameters) {
        if (data == null || data.isEmpty()) return;

        // 1. 获取所有有效字段
        Field[] allFields = data.get(0).getClass().getDeclaredFields();
        List<Field> candidateFields = new ArrayList<>();
        for (Field f : allFields) {
            if (!Modifier.isStatic(f.getModifiers()) && !Modifier.isFinal(f.getModifiers())) {
                f.setAccessible(true);
                candidateFields.add(f);
            }
        }

        // 2. 扫描非空列
        List<Field> effectiveFields = new ArrayList<>();
        try {
            for (Field field : candidateFields) {
                boolean hasValue = false;
                for (T item : data) {
                    if (field.get(item) != null) {
                        hasValue = true;
                        break;
                    }
                }
                if (hasValue) effectiveFields.add(field);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("字段访问异常", e);
        }

        // 3. 构建 SQL
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
        StringBuilder placeholders = new StringBuilder("VALUES (");
        for (int i = 0; i < effectiveFields.size(); i++) {
            sql.append(camelToUnderline(effectiveFields.get(i).getName()));
            placeholders.append("?");
            if (i < effectiveFields.size() - 1) {
                sql.append(", ");
                placeholders.append(", ");
            }
        }
        sql.append(") ").append(placeholders).append(")");


        // 打印 SQL 骨架（类似 MP 的 Preparing）
        log.info("==> Preparing: {}", sql);

        // 4. 执行写入
        jdbcTemplate.execute((Connection conn) -> {
            boolean initialAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int count = 0;
                List<T> currentBatch = new ArrayList<>(); // 用于记录当前批次的数据供日志打印

                for (T item : data) {
                    List<Object> params = new ArrayList<>();
                    for (int i = 0; i < effectiveFields.size(); i++) {
                        Object val = effectiveFields.get(i).get(item);
                        ps.setObject(i + 1, val);
                        params.add(val);
                    }

                    // 打印参数（类似 MP 的 Parameters，建议只在 DEBUG 模式或数据量小时打印）
                    if (log.isDebugEnabled() && isPrintParameters && currentBatch.size() < 100) {
                        log.debug("==> Parameters: {}", params.stream()
                                .map(v -> v == null ? "null" : v + "(" + v.getClass().getSimpleName() + ")")
                                .collect(Collectors.joining(", ")));
                    }

                    ps.addBatch();
                    count++;

                    if (count % batchSize == 0) {
                        ps.executeBatch();
                        log.info("==> Batch Flush: {} records committed", count);
                    }
                }
                ps.executeBatch();
                conn.commit();
                log.info("==> Total Success: {} records", count);
            } catch (Exception e) {
                conn.rollback();
                log.error("==> Batch Error: ", e);
                throw new RuntimeException(e);
            } finally {
                conn.setAutoCommit(initialAutoCommit);
            }
            return null;
        });
    }

    private static String camelToUnderline(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append("_").append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}