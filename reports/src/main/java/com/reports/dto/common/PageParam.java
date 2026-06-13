package com.reports.dto.common;

import lombok.Data;

/**
 * 分页参数
 */
@Data
public class PageParam {

    /**
     * 当前页码（默认第1页）
     */
    private Integer page = 1;

    /**
     * 每页条数（默认10条）
     */
    private Integer pageSize = 10;

    /**
     * 获取 offset
     */
    public Integer getOffset() {
        return (page - 1) * pageSize;
    }

}
