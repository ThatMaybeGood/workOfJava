package com.reports.dto.request;

import com.reports.dto.common.BaseRequestBody;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 人员字典查询请求体
 * method: reports.common.staff-dict
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StaffDictRequest extends BaseRequestBody {

    private static final long serialVersionUID = 1L;

    /**
     * 所属科室代码（可选）
     */
    private String deptCode;

    /**
     * 人员姓名（支持模糊匹配，可选）
     */
    private String staffName;
}
