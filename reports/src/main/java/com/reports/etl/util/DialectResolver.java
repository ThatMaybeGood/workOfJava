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

    public static String getUpsertSql(String targetTable, String[] indexCols, String[] updateCols,
                                      String[] allCols, String dialect) {
        StringBuilder sql = new StringBuilder();
        if ("ORACLE".equals(dialect) || "DM".equals(dialect) || "SQLSERVER".equals(dialect)) {
            sql.append("MERGE INTO ").append(targetTable).append(" USING (SELECT ");
            for (int i = 0; i < allCols.length; i++) {
                if (i > 0) sql.append(", ");
                sql.append(":").append(allCols[i]).append(" AS ").append(allCols[i]);
            }
            sql.append(" FROM DUAL) src ON (");
            for (int i = 0; i < indexCols.length; i++) {
                if (i > 0) sql.append(" AND ");
                sql.append("src.").append(indexCols[i]).append(" = ").append(targetTable).append(".").append(indexCols[i]);
            }
            sql.append(") WHEN MATCHED THEN UPDATE SET ");
            for (int i = 0; i < updateCols.length; i++) {
                if (i > 0) sql.append(", ");
                sql.append(targetTable).append(".").append(updateCols[i]).append(" = src.").append(updateCols[i]);
            }
            sql.append(" WHEN NOT MATCHED THEN INSERT (");
            for (int i = 0; i < allCols.length; i++) {
                if (i > 0) sql.append(", ");
                sql.append(allCols[i]);
            }
            sql.append(") VALUES (");
            for (int i = 0; i < allCols.length; i++) {
                if (i > 0) sql.append(", ");
                sql.append(":").append(allCols[i]);
            }
            sql.append(")");
        } else if ("MYSQL".equals(dialect)) {
            sql.append("INSERT INTO ").append(targetTable).append(" (");
            for (int i = 0; i < allCols.length; i++) {
                if (i > 0) sql.append(", ");
                sql.append(allCols[i]);
            }
            sql.append(") VALUES (");
            for (int i = 0; i < allCols.length; i++) {
                if (i > 0) sql.append(", ");
                sql.append(":").append(allCols[i]);
            }
            sql.append(") ON DUPLICATE KEY UPDATE ");
            for (int i = 0; i < updateCols.length; i++) {
                if (i > 0) sql.append(", ");
                sql.append(updateCols[i]).append(" = VALUES(").append(updateCols[i]).append(")");
            }
        } else if ("POSTGRESQL".equals(dialect)) {
            sql.append("INSERT INTO ").append(targetTable).append(" (");
            for (int i = 0; i < allCols.length; i++) {
                if (i > 0) sql.append(", ");
                sql.append(allCols[i]);
            }
            sql.append(") VALUES (");
            for (int i = 0; i < allCols.length; i++) {
                if (i > 0) sql.append(", ");
                sql.append(":").append(allCols[i]);
            }
            sql.append(") ON CONFLICT (");
            for (int i = 0; i < indexCols.length; i++) {
                if (i > 0) sql.append(", ");
                sql.append(indexCols[i]);
            }
            sql.append(") DO UPDATE SET ");
            for (int i = 0; i < updateCols.length; i++) {
                if (i > 0) sql.append(", ");
                sql.append(updateCols[i]).append(" = EXCLUDED.").append(updateCols[i]);
            }
        } else {
            sql.append("MERGE INTO ").append(targetTable).append(" USING (SELECT ");
            for (int i = 0; i < allCols.length; i++) {
                if (i > 0) sql.append(", ");
                sql.append(":").append(allCols[i]).append(" AS ").append(allCols[i]);
            }
            sql.append(" FROM DUAL) src ON (");
            for (int i = 0; i < indexCols.length; i++) {
                if (i > 0) sql.append(" AND ");
                sql.append("src.").append(indexCols[i]).append(" = ").append(targetTable).append(".").append(indexCols[i]);
            }
            sql.append(") WHEN MATCHED THEN UPDATE SET ");
            for (int i = 0; i < updateCols.length; i++) {
                if (i > 0) sql.append(", ");
                sql.append(targetTable).append(".").append(updateCols[i]).append(" = src.").append(updateCols[i]);
            }
            sql.append(" WHEN NOT MATCHED THEN INSERT (");
            for (int i = 0; i < allCols.length; i++) {
                if (i > 0) sql.append(", ");
                sql.append(allCols[i]);
            }
            sql.append(") VALUES (");
            for (int i = 0; i < allCols.length; i++) {
                if (i > 0) sql.append(", ");
                sql.append(":").append(allCols[i]);
            }
            sql.append(")");
        }
        return sql.toString();
    }

}