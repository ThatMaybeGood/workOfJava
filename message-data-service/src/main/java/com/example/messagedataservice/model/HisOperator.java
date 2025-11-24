package com.example.messagedataservice.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Date;

@Data
public class HisEmployee {
    //员工ID
    private Long operatorNo;
    @NotBlank(message = "员工姓名不能为空")
    @Size(min = 2, max = 50, message = "员工姓名长度必须在2-50字符之间")
    private String operatorName;
    //部门ID
    private Integer departmentId;
    //是否有效（0:无效，1：有效）
    Boolean isValid;
    //创建人
    String creator;
    //创建时间
    Date createTime;
    //更新人
    String updater;
    //更新时间
    Date updateTime;

}
