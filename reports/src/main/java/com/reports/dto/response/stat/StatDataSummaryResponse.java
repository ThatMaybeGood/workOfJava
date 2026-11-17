package com.reports.dto.response.stat;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 运营数据统计 - 响应体(13个页签通用结构)
 */
@Data
public class StatDataSummaryResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 指标卡片(无卡片的页签为空)
     */
    private List<StatCard> cards;

    /**
     * 表格列定义(key=行数据键, label=表头)
     */
    private List<StatColumn> columns;

    /**
     * 表格行
     */
    private List<Map<String, Object>> list;

    /**
     * 总行数(仅分页页签有意义)
     */
    private long total;

    /**
     * 当前页码
     */
    private int page;

    /**
     * 每页条数
     */
    private int pageSize;

    /**
     * 是否分页页签
     */
    private boolean paged;

    /**
     * 指标卡片
     */
    @Data
    public static class StatCard implements Serializable {
        private static final long serialVersionUID = 1L;
        /** 指标名 */
        private String label;
        /** 指标值 */
        private String value;
        /** 单位(如 分钟/个, 可空) */
        private String unit;

        public StatCard() {
        }

        public StatCard(String label, String value, String unit) {
            this.label = label;
            this.value = value;
            this.unit = unit;
        }
    }

    /**
     * 表格列定义
     */
    @Data
    public static class StatColumn implements Serializable {
        private static final long serialVersionUID = 1L;
        /** 行数据键 */
        private String key;
        /** 表头文案 */
        private String label;

        public StatColumn() {
        }

        public StatColumn(String key, String label) {
            this.key = key;
            this.label = label;
        }
    }

}
