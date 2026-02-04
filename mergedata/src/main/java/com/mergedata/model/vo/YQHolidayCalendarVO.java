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
     * 节假日类型，周末，节假日，工作日 0，1，2
     */
    private String holidayType;
    /**
     * 查询类型，0 门诊 1住院
     */
    private String queryType;
}
