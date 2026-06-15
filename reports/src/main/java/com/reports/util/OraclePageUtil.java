package com.reports.util;

/**
 * Oracle 分页 SQL 工具类
 *
 * 支持两种分页方式：
 * 1. ROWNUM 方式（Oracle 11g 及以下）
 * 2. OFFSET FETCH 方式（Oracle 12c+）
 */
public class OraclePageUtil {

    /**
     * Oracle 11g 及以下版本分页（三层嵌套 ROWNUM）
     *
     * @param sql      原始 SQL（不含分页）
     * @param page     当前页码（从1开始）
     * @param pageSize 每页条数
     * @return 包装后的分页 SQL
     */
    public static String wrapRowNumPage(String sql, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return "SELECT * FROM (" +
                "  SELECT t.*, ROWNUM rn FROM (" + sql +
                "  ) t WHERE ROWNUM <= " + (offset + pageSize) +
                ") WHERE rn > " + offset;
    }

    /**
     * Oracle 12c+ 版本分页（OFFSET FETCH）
     *
     * @param sql      原始 SQL（不含分页）
     * @param page     当前页码（从1开始）
     * @param pageSize 每页条数
     * @return 包装后的分页 SQL
     */
    public static String wrapOffsetFetchPage(String sql, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return sql + " OFFSET " + offset + " ROWS FETCH NEXT " + pageSize + " ROWS ONLY";
    }

    /**
     * 计算 offset
     *
     * @param page     当前页码
     * @param pageSize 每页条数
     * @return offset 值
     */
    public static int calcOffset(int page, int pageSize) {
        return (page - 1) * pageSize;
    }

    /**
     * 计算总页数
     *
     * @param total    总记录数
     * @param pageSize 每页条数
     * @return 总页数
     */
    public static int calcTotalPages(long total, int pageSize) {
        return (int) Math.ceil((double) total / pageSize);
    }

}
