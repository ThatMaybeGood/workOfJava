package com.reports.dto.request;

import com.reports.dto.common.BaseRequestBody;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

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
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startDate;

    /**
     * 结束日期，格式 yyyy-MM-dd
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endDate;

    /**
     * 科室名称（可选）
     */
    private String deptName;

}
