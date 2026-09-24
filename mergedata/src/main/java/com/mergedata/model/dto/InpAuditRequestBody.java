package com.mergedata.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 入参：住院报表审核
 */
@Data
public class InpAuditRequestBody {

    @NotNull(message = "reportdate不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @JsonProperty("reportdate")
    private LocalDate reportDate;

    /**
     * 是否汇总 0：非汇总 1：汇总
     */
    @JsonProperty("total_flag")
    private String totalFlag;

    /**
     * 审核动作 1：审核通过 2：审核不通过 0：取消审核
     */
    @NotNull(message = "audit_status不能为空")
    @JsonProperty("audit_status")
    private String auditStatus;

    /**
     * 审核意见（审核不通过时必填）
     */
    @JsonProperty("audit_remark")
    private String auditRemark;

    /**
     * 审核人
     */
    @JsonProperty("audit_by")
    private String auditBy;

}
