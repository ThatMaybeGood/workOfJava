package com.reports.dto.request;

import com.reports.dto.common.BaseRequestBody;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 医保对账每日对比请求体
 * method: reports.cash.mi-recon
 * action: compare(对账对比)/records(上传记录)/updateType(修改类型)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MiReconRequest extends BaseRequestBody {

    private static final long serialVersionUID = 1L;

    /**
     * 动作: compare/records/updateType, 默认compare
     */
    private String action;

    /**
     * compare: 医保类型 310/390/3101/3901, 必填
     */
    private String insurType;

    /**
     * compare: 对账月份, 格式 yyyy-MM, 默认当月; 后端折算为当月1号~月末
     */
    private String month;

    /**
     * updateType: 上传记录id
     */
    private Long id;
}
