package com.reports.dto.response.outpatient.operation;

import lombok.Data;

/**
 * 门诊运行数据 - 表格行数据
 */
@Data
public class TableItem {

    /**
     * 科室代码
     */
    private String deptCode;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 门诊量
     */
    private Integer totalVisits;

    /**
     * 预约挂号率
     */
    private String appointmentRate;

    /**
     * 出诊人次
     */
    private Integer visitCount;

    /**
     * 预约诊察率
     */
    private String examRate;

    /**
     * 接诊效率
     */
    private Double efficiency;

    /**
     * 有效出诊单元数
     */
    private Integer effectiveUnits;

    /**
     * 出诊单元总数
     */
    private Integer totalUnits;

    /**
     * 出诊人次-名医专家
     */
    private Integer famousExpert;

    /**
     * 出诊人次-特需专家
     */
    private Integer specialExpert;

    /**
     * 出诊人次-知名专家
     */
    private Integer knownExpert;

    /**
     * 出诊人次-专家A
     */
    private Integer expertA;

    /**
     * 出诊人次-专家B
     */
    private Integer expertB;

    /**
     * 出诊人次-普通门诊
     */
    private Integer ordinary;

    /**
     * 有效出诊单元-名医
     */
    private Integer unitFamousEffective;

    /**
     * 出诊单元-名医
     */
    private Integer unitFamousTotal;

    /**
     * 有效出诊单元-特需
     */
    private Integer unitSpecialEffective;

    /**
     * 出诊单元-特需
     */
    private Integer unitSpecialTotal;

    /**
     * 有效出诊单元-知名专家
     */
    private Integer unitKnownEffective;

    /**
     * 出诊单元-知名专家
     */
    private Integer unitKnownTotal;

    /**
     * 有效出诊单元-专家A
     */
    private Integer unitAEffective;

    /**
     * 出诊单元-专家A
     */
    private Integer unitATotal;

    /**
     * 有效出诊单元-专家B
     */
    private Integer unitBEffective;

    /**
     * 出诊单元-专家B
     */
    private Integer unitBTotal;

    /**
     * 有效出诊单元-普通
     */
    private Integer unitOrdinaryEffective;

    /**
     * 出诊单元-普通
     */
    private Integer unitOrdinaryTotal;

}
