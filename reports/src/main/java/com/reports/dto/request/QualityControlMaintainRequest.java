package com.reports.dto.request;

import com.reports.dto.common.BaseRequestBody;
import com.reports.dto.response.outpatient.quality.control.QcMaintainItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 门诊管理质量控制数据维护请求体
 * method: reports.outp.quality-control-maintain
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QualityControlMaintainRequest extends BaseRequestBody {

    private static final long serialVersionUID = 1L;

    /**
     * 操作类型：query（查询）、save（保存）
     */
    private String action;

    /**
     * 统计月份，格式 yyyy-MM
     */
    private String statMonth;

    /**
     * 保存的指标列表（save 时传入，按 月份+指标编码 覆盖）
     */
    private List<QcMaintainItem> list;
}
