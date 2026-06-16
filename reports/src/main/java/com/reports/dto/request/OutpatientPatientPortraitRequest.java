package com.reports.dto.request;

import com.reports.dto.common.BaseRequestBody;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 患者画像 - 请求体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OutpatientPatientPortraitRequest extends BaseRequestBody {

    private static final long serialVersionUID = 1L;

    /**
     * 患者类型：outpatient（门诊患者）、inpatient（住院患者）
     */
    private String patientType;

    /**
     * 统计时间范围
     */
    private String timeRange;

    /**
     * 开始日期，格式 yyyy-MM-dd
     */
    private String startDate;

    /**
     * 结束日期，格式 yyyy-MM-dd
     */
    private String endDate;

    /**
     * 科室名称（可选）
     */
    private String deptName;

}
