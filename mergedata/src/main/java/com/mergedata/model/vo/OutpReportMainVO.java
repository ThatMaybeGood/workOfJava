package com.mergedata.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 门诊现金统计主表VO
 */
@Data
public class OutpReportMainVO {
    /**
     * 汇总标志
     */
    private String totalFlag;

    /**
     * 报表list
     */
    @JsonProperty("list")
    @TableField(exist = false)
    private List<OutpReportSubVO> subList;
}
