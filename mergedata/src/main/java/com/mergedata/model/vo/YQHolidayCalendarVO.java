package com.mergedata.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 节假日日历VO
 */
@Data
public class YQHolidayCalendarVO {
    /**
     * 节假日日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "节假日日期不能为空")
    private LocalDate holidayDate;
    /**
     * 节假日类型， 0 工作日 1 节假日  2 节假日后第一天 3 节假日前一天 4 月末最后一天且是节假日 5 月末最后一天且非节假日
     */
    private String holidayType;
    /**
     * 查询类型，0 门诊 1住院
     */
    private String queryType;
    /**
     * 回溯汇总时候的截止日期
     */
    private LocalDate minDate;

     /**
     * 汇总标志，0 不汇总 1 汇总 2 月初
     */
    private String totalFlag;

    /**
     * 汇总标题
     */
     private String totalTitle;




}
