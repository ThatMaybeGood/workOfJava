package com.mergedata.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @LocalDate 2025/10/30 10:46
 */

/**
 * 节假日实体类
 */
@Data
public class YQHolidayEntity {
    /**
     * 流水号
     */
    private String serialNo;
    /**
     * 节假日日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "节假日日期不能为空")
    private LocalDate holidayDate;
    /**
     * 有效状态
     */
    private String validStatus;
    /**
     * 节假日类型  周末，节假日，工作日 0，1，2
     */
    private String holidayType;
    /**
     * 节假日年份
     */
     private String holidayYear;
    /**
     * 节假日月份
     */
    private String holidayMonth;
     /**
      * 创建人
      */
    private String creator;
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    /**
     * 更新时间
     */
    private LocalDate updateTime;
    /**
     * 更新次数
     */
    private String updateCount;
    /**
     * 备注
     */
    private String remark;
    /**
     * 节假日分类 门诊/住院/通用 0/1/2
     */
    private String category;
}