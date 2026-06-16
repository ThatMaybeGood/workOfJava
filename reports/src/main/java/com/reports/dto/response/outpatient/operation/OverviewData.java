package com.reports.dto.response.outpatient.operation;

import lombok.Data;

/**
 * 门诊运行数据 - 概览数据
 */
@Data
public class OverviewData {

    /**
     * 总就诊人次
     */
    private Integer totalVisits;

    /**
     * 预约率
     */
    private String appointmentRate;

    /**
     * 就诊人次
     */
    private Integer visitCount;

    /**
     * 就诊人次明细
     */
    private VisitCountDetail visitCountDetail;

    /**
     * 检查率
     */
    private String examRate;

    /**
     * 效率
     */
    private Double efficiency;

    /**
     * 有效单元数
     */
    private Integer effectiveUnits;

    /**
     * 总单元数
     */
    private Integer totalUnits;

    /**
     * 单元明细
     */
    private UnitDetail unitDetail;

}
