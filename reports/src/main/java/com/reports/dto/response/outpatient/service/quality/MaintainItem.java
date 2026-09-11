package com.reports.dto.response.outpatient.service.quality;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 门诊服务质量数据维护明细项
 */
@Data
public class MaintainItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID（新增时为空）
     */
    private Long id;

    /**
     * 发生时间（投诉时间/表扬时间）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date time;

    /**
     * 科室代码
     */
    private String deptCode;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 人员姓名
     */
    private String personName;

    /**
     * 岗位类别
     */
    private String position;

    /**
     * 投诉分类
     */
    private String category;

    /**
     * 处理结果
     */
    private String result;

    /**
     * 表扬方式
     */
    private String method;

    /**
     * 是否反馈科室
     */
    private String feedback;

    /**
     * 备注
     */
    private String remark;
}
