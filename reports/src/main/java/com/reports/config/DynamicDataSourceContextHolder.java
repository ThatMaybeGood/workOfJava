package com.reports.config;

/**
 * 动态数据源上下文
 */
public class DynamicDataSourceContextHolder {

    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 默认数据源
     */
    public static final String DEFAULT_DS = "master";

    /**
     * 设置当前数据源
     */
    public static void set(String dataSource) {
        CONTEXT_HOLDER.set(dataSource);
    }

    /**
     * 获取当前数据源
     */
    public static String get() {
        String ds = CONTEXT_HOLDER.get();
        return ds == null ? DEFAULT_DS : ds;
    }

    /**
     * 清除当前数据源
     */
    public static void clear() {
        CONTEXT_HOLDER.remove();
    }

}
