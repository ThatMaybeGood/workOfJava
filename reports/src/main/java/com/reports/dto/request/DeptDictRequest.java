package com.reports.dto.request;

import com.reports.dto.common.BaseRequestBody;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 科室字典查询请求体
 * method: reports.common.dept-dict
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeptDictRequest extends BaseRequestBody {

    private static final long serialVersionUID = 1L;

    /**
     * 科室类型：0 门诊，1 住院，2 其他
     */
    private Integer deptType;

    /**
     * 科室编码；0000 表示全部，其余按返回的对应科室代码
     */
    private String deptCode;

    /**
     * 科室名称（支持模糊匹配，可选）
     */
    private String deptName;

    /**
     * 扩展参数 1（可按业务需要命名，例如 parentCode / level / deptAttr）
     */
    private String extField1;

    /**
     * 扩展参数 2
     */
    private String extField2;

    /**
     * 扩展参数 3
     */
    private String extField3;
}
