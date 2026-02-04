package com.mergedata.model.entity;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;

/**
 * 医院操作员实体类
 */
@Data
public class YQOperatorEntity {
    /**
     * 流水号
     */
    private String serialNo;
    /**
     * 员工ID
     */
    @NotBlank(message = "员工ID不能为空")
    private String operatorNo;
    /**
     * 员工姓名
     */
    @NotBlank(message = "员工姓名不能为空")
    @Size(min = 2, max = 50, message = "员工姓名长度必须在2-50字符之间")
    private String operatorName;
    /**
     * 员工类型 门诊/住院 0/1
     */
    private String category;

    /**
     * 部门ID
     */
    private String departmentId;
    /**
     * 是否有效（0:无效，1：有效）
     */
    private  String validStatus;
    /**
     * 创建人
     */
    private String creator;
    /**
     * 创建时间
     */
    private LocalDate createTime;
    /**
     * 更新人
     */
    private String updator;
    /**
     * 更新时间
     */
    private LocalDate updateTime;
    /**
     * 更新次数
     */
    private Integer updateCount;
    /**
     * 序号
     */
    private Integer rowNum;
    /**
     * 是否在窗内（0:否，1：是）
     */
    private Boolean inpWindow;
    /**
     * 是否在ATM机内（0:否，1：是）
     */
    private Boolean atm;

}
