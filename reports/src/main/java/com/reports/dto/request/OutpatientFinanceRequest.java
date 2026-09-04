package com.reports.dto.request;

import com.reports.dto.common.BaseRequestBody;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 门诊财务报表 - 请求体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OutpatientFinanceRequest extends BaseRequestBody {

    private static final long serialVersionUID = 1L;

    /**
     * 统计类型：1汇总 2进项 3退项
     */
    private Integer statisticType;

    /**
     * 时间类型：1按月 2按天
     */
    private Integer timeType;

    /**
     * 开始时间（月模式 yyyy-MM，天模式 yyyy-MM-dd）
     */
    private String startDate;

    /**
     * 结束时间（月模式 yyyy-MM，天模式 yyyy-MM-dd）
     */
    private String endDate;

    /**
     * 按需查询的饼图业务类型（逗号分隔，如 "1,2,3"）。
     * 不传表示查全部；传入时后端只返回这些类型的饼图数据（仅 pieList 有值）
     */
    private String pieTypes;

}
