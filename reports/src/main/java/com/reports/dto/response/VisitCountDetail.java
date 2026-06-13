package com.reports.dto.response;

import lombok.Data;

/**
 * 就诊人次明细
 */
@Data
public class VisitCountDetail {

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

}
