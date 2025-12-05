package com.mergedata.model;


import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;


@Data
public class YQOperator {

    private String serialNo;
    //员工ID
    private String operatorNo;

    @NotBlank(message = "员工姓名不能为空")
    @Size(min = 2, max = 50, message = "员工姓名长度必须在2-50字符之间")
    private String operatorName;
    //部门ID
    private String departmentId;
    //是否有效（0:无效，1：有效）
    String isValid;
    //创建人
        String creator;
    //创建时间
    LocalDate createTime;
    //更新人
    String updator;
    //更新时间
    LocalDate updateTime;

    Integer updateCount;

}
