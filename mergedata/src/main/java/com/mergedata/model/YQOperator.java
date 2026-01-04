package com.mergedata.model;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;


@Data
public class YQOperator {

    @Schema(description = "流水号")
    private String serialNo;
    //员工ID
    @Schema(description = "员工ID")
    @NotBlank(message = "员工ID不能为空")
    private String operatorNo;

    @NotBlank(message = "员工姓名不能为空")
    @Schema(description = "员工姓名")
    @Size(min = 2, max = 50, message = "员工姓名长度必须在2-50字符之间")
    private String operatorName;
    //部门ID
    private String departmentId;
    //是否有效（0:无效，1：有效）
    private  String validStatus;
    //创建人
    private String creator;
    //创建时间
    private LocalDate createTime;
    //更新人
    private String updator;
    //更新时间
    private LocalDate updateTime;

    private Integer updateCount;

    private Integer rowNum;

    private Boolean inpWindow;

    private Boolean ATM;

}
