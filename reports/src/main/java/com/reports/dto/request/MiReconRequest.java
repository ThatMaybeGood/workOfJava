package com.reports.dto.request;

import com.reports.dto.common.BaseRequestBody;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 医保对账每日对比请求体
 * method: reports.cash.mi-recon
 * action: compare(对账对比)/records(上传记录)/updateType(修改类型)/zpDiff(账平差异明细)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MiReconRequest extends BaseRequestBody {

    private static final long serialVersionUID = 1L;

    /**
     * 动作: compare/records/updateType/zpDiff/unmatchedNeg/enableFile/deleteFile/payComp, 默认compare
     */
    private String action;

    /**
     * payComp: 维度 sum(险种月合计)/cat(险种+类别)/day(按日)/dayCat(按日+类别)/cross(退费跨月配对)
     */
    private String dim;

    /**
     * compare/zpDiff: 医保类型 310/390/3101/3901, 必填
     */
    private String insurType;

    /**
     * compare: 对账月份, 格式 yyyy-MM, 默认当月; 后端折算为当月1号~月末
     */
    private String month;

    /**
     * zpDiff: 结算日期, 格式 yyyy-MM-dd, 必填
     */
    private String settleDate;

    /**
     * records: 数据日期筛选, 格式 yyyy-MM-dd, 空为全部
     */
    private String dataDate;

    /**
     * records: 页码, 默认1
     */
    private Integer pageNum;

    /**
     * records: 每页条数, 默认10
     */
    private Integer pageSize;

    /**
     * updateType: 上传记录id
     */
    private Long id;
}
