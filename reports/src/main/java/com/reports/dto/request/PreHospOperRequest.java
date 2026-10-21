package com.reports.dto.request;

import com.reports.dto.common.BaseRequestBody;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 院前操作员维护请求体
 * method: reports.cash.pre-hosp-oper
 * action: query(维护列表)/search(人员搜索)/add/delete/setValid
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PreHospOperRequest extends BaseRequestBody {

    private static final long serialVersionUID = 1L;

    /**
     * 动作: query/search/add/delete/setValid, 默认query
     */
    private String action;

    /**
     * search: 姓名/登录账号关键字
     */
    private String keyword;

    /**
     * search: 页码(1起), 默认1
     */
    private Integer page;

    /**
     * search: 每页条数, 默认10
     */
    private Integer pageSize;

    /**
     * add: 手动录入的操作员姓名(展示仍以HIS users为准, 此字段仅作录入校验提示)
     */
    private String operName;

    /**
     * add: 操作员=USER_ID(统计过滤用)
     */
    private String operNo;

    /**
     * add: 登录ID=DB_USER(维护显示用)
     */
    private String dbuser;

    /**
     * delete/setValid: 记录ID
     */
    private Long id;

    /**
     * setValid: 1有效 0无效
     */
    private Integer validFlag;
}
