package com.reports.dto.response.outpatient.operation;

import lombok.Data;

/**
 * 门诊运行数据 - 表格行数据
 */
@Data
public class TableItem {

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 就诊人次
     */
    private Integer visits;

    /**
     * 预约率
     */
    private String appointmentRate;

    /**
     * 检查率
     */
    private String examRate;

    /**
     * 效率
     */
    private Double efficiency;

    /**
     * 就诊人次统计
     */
    private Integer visitCount;

    /**
     * 名医
     */
    private Integer famousExpert;

    /**
     * 特需专家
     */
    private Integer specialExpert;

    /**
     * 知名专家
     */
    private Integer knownExpert;

    /**
     * 专家A
     */
    private Integer expertA;

    /**
     * 专家B
     */
    private Integer expertB;

    /**
     * 普通
     */
    private Integer ordinary;

    /**
     * 有效单元总数
     */
    private UnitDetailItem effectiveUnitsTotal;

    /**
     * 单元明细
     */
    private UnitDetail unitDetail;

}
