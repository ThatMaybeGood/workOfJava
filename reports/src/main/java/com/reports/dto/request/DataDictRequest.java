package com.reports.dto.request;

import com.reports.dto.common.BaseRequestBody;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通用字典请求体
 * method: reports.common.data-dict
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DataDictRequest extends BaseRequestBody {

    private static final long serialVersionUID = 1L;

    /**
     * 操作类型：query（查询）、add（新增）、delete（删除）
     */
    private String action;

    /**
     * 字典类型：position/complaintCategory/complaintResult/praiseMethod/feedback
     */
    private String dictType;

    /**
     * 字典编码
     */
    private String dictCode;

    /**
     * 字典名称
     */
    private String dictName;

    /**
     * 排序号
     */
    private Integer sortNo;

    /**
     * 主键ID（删除时传入）
     */
    private Long id;
}
