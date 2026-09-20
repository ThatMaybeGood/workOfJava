package com.reports.dto.request;

import com.reports.dto.common.BaseRequestBody;
import com.reports.dto.response.outpatient.internet.hospital.IhMaintainItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 互医质控运营月报数据维护请求体
 * method: reports.outp.internet-hospital-maintain
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InternetHospitalMaintainRequest extends BaseRequestBody {

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
    private List<IhMaintainItem> list;
}
