package com.reports.etl.util;

import java.util.*;

public class DialectResolver {

    private static final Map<String, String> DRIVER_TO_DIALECT = new HashMap<>();
    private static final Map<String, String> DB_TYPE_TO_DIALECT = new HashMap<>();

    static {
        DRIVER_TO_DIALECT.put("oracle.jdbc.OracleDriver", "ORACLE");
        DRIVER_TO_DIALECT.put("com.mysql.cj.jdbc.Driver", "MYSQL");
        DRIVER_TO_DIALECT.put("com.mysql.jdbc.Driver", "MYSQL");
        DRIVER_TO_DIALECT.put("org.postgresql.Driver", "POSTGRESQL");
        DRIVER_TO_DIALECT.put("com.microsoft.sqlserver.jdbc.SQLServerDriver", "SQLSERVER");
        DRIVER_TO_DIALECT.put("dm.jdbc.driver.DmDriver", "DM");
        DRIVER_TO_DIALECT.put("org.h2.Driver", "H2");
    }

    public static String resolve(String dbType, String driverClass, String jdbcUrl) {
        if (dbType != null && !dbType.isEmpty()) {
            return dbType.toUpperCase();
        }
        if (driverClass != null) {
            String d = DRIVER_TO_DIALECT.get(driverClass.trim());
            if (d != null) return d;
        }
        if (jdbcUrl != null) {
            String lower = jdbcUrl.toLowerCase();
            if (lower.contains("mysql")) return "MYSQL";
            if (lower.contains("postgresql") || lower.contains("postgres")) return "POSTGRESQL";
            if (lower.contains("sqlserver")) return "SQLSERVER";
            if (lower.contains("dm")) return "DM";
            if (lower.contains("h2")) return "H2";
            if (lower.contains("oracle")) return "ORACLE";
        }
        return "ORACLE";
    }

    /**
     * 生成 UPSERT SQL（全部使用 ? 位置参数，PreparedStatement 直接绑定）
     * 参数顺序约定：
     * - MERGE 方言（ORACLE/DM/SQLSERVER/H2）：allCols 绑两遍（USING 子查询一遍 + INSERT VALUES 一遍）
     * - MYSQL / POSTGRESQL：allCols 绑一遍
     */
    public static String getUpsertSql(String targetTable, String[] indexCols, String[] updateCols,
                                      String[] allCols, String dialect) {
        StringBuilder sql = new StringBuilder();
        if ("MYSQL".equals(dialect)) {
            sql.append("INSERT INTO ").append(targetTable).append(" (");
            sql.append(String.join(", ", allCols));
            sql.append(") VALUES (").append(placeholders(allCols.length));
            sql.append(") ON DUPLICATE KEY UPDATE ");
            for (int i = 0; i < updateCols.length; i++) {
                if (i > 0) sql.append(", ");
                sql.append(updateCols[i]).append(" = VALUES(").append(updateCols[i]).append(")");
            }
        } else if ("POSTGRESQL".equals(dialect)) {
            sql.append("INSERT INTO ").append(targetTable).append(" (");
            sql.append(String.join(", ", allCols));
            sql.append(") VALUES (").append(placeholders(allCols.length));
            sql.append(") ON CONFLICT (").append(String.join(", ", indexCols)).append(") DO UPDATE SET ");
            for (int i = 0; i < updateCols.length; i++) {
                if (i > 0) sql.append(", ");
                sql.append(updateCols[i]).append(" = EXCLUDED.").append(updateCols[i]);
            }
        } else if ("H2".equals(dialect)) {
            // H2 传统语法：MERGE INTO ... KEY(...) VALUES(...)，命中 KEY 则整行更新，否则插入
            sql.append("MERGE INTO ").append(targetTable).append(" (");
            sql.append(String.join(", ", allCols));
            sql.append(") KEY (").append(String.join(", ", indexCols));
            sql.append(") VALUES (").append(placeholders(allCols.length)).append(")");
        } else {
            // ORACLE / DM / SQLSERVER / H2：标准 MERGE INTO
            sql.append("MERGE INTO ").append(targetTable).append(" USING (SELECT ");
            for (int i = 0; i < allCols.length; i++) {
                if (i > 0) sql.append(", ");
                sql.append("? AS ").append(allCols[i]);
            }
            sql.append(" FROM DUAL) src ON (");
            for (int i = 0; i < indexCols.length; i++) {
                if (i > 0) sql.append(" AND ");
                sql.append("src.").append(indexCols[i]).append(" = ").append(targetTable).append(".").append(indexCols[i]);
            }
            sql.append(")");
            if (updateCols.length > 0) {
                sql.append(" WHEN MATCHED THEN UPDATE SET ");
                for (int i = 0; i < updateCols.length; i++) {
                    if (i > 0) sql.append(", ");
                    sql.append(targetTable).append(".").append(updateCols[i]).append(" = src.").append(updateCols[i]);
                }
            }
            sql.append(" WHEN NOT MATCHED THEN INSERT (");
            sql.append(String.join(", ", allCols));
            sql.append(") VALUES (").append(placeholders(allCols.length)).append(")");
        }
        return sql.toString();
    }

    /** MERGE 方言（ORACLE/DM/SQLSERVER）需要绑定两遍 allCols（USING + INSERT VALUES），其余一遍 */
    public static boolean upsertBindsTwice(String dialect) {
        return "ORACLE".equals(dialect) || "DM".equals(dialect) || "SQLSERVER".equals(dialect);
    }

    private static String placeholders(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append(", ");
            sb.append("?");
        }
        return sb.toString();
    }

}