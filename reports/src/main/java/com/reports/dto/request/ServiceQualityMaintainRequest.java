package com.reports.dto.request;

import com.reports.dto.common.BaseRequestBody;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.reports.dto.response.outpatient.service.quality.MaintainItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * 门诊服务质量数据维护请求体
 * method: reports.outp.service-quality-maintain
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ServiceQualityMaintainRequest extends BaseRequestBody {

    private static final long serialVersionUID = 1L;

    /**
     * 操作类型：query（查询）、save（保存）、delete（删除）
     */
    private String action;

    /**
     * 明细类型：complaint（投诉）、praise（表扬）
     */
    private String type;

    /**
     * 主键ID（删除时传入）
     */
    private Long id;

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
     * 当前页码（可选，默认第1页）
     */
    private Integer page;

    /**
     * 每页条数（可选，默认10条）
     */
    private Integer pageSize;

    /**
     * 保存的明细列表（save 时传入，id 为空表示新增）
     */
    private List<MaintainItem> list;
}
