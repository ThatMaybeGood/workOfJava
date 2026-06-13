package com.reports.dto.response;

import lombok.Data;

/**
 * 单元明细（各专家级别）
 */
@Data
public class UnitDetail {

    private UnitDetailItem famousExpert;
    private UnitDetailItem specialExpert;
    private UnitDetailItem knownExpert;
    private UnitDetailItem expertA;
    private UnitDetailItem expertB;
    private UnitDetailItem ordinary;

}
