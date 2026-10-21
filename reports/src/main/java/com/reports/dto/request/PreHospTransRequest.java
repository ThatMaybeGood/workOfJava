package com.reports.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.reports.dto.common.BaseRequestBody;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 院前转住院统计请求体
 * method: reports.cash.pre-hosp-trans
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PreHospTransRequest extends BaseRequestBody {

    private static final long serialVersionUID = 1L;

    /**
     * 时间粒度: day(按天)/month(按月)
     */
    private String timeDimension;

    /**
     * 收费员下拉筛选: 操作员(user_id), 空=全部已维护操作员
     */
    private String operatorNo;

    /**
     * 开始日期, 格式 yyyy-MM-dd
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startDate;

    /**
     * 结束日期, 格式 yyyy-MM-dd
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endDate;
}
