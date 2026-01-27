package com.mergedata.model.dto;

import lombok.Data;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2025/10/30 11:01
 */ // 统一返回结果
@Data
public class ResultDTO<T> {
    private String sign_type;
    private String sign;
    private int code;
    private String msg;
    private String sub_code;
    private String sub_msg;
    private T list;

    public static <T> ResultDTO<T> success(T data) {
        ResultDTO<T> resultDTO = new ResultDTO<>();
        resultDTO.setSign_type("md5");
        resultDTO.setSign("s1tTc4F8ZYzdJ7HkNUJkZw==");
        resultDTO.setCode(10000);
        resultDTO.setMsg("接口调用成功，并且业务系统也处理成功");
        resultDTO.setSub_code("success");
        resultDTO.setSub_msg("查询报表信息成功！");
        resultDTO.setList(data);
        return resultDTO;
    }

    public static <T> ResultDTO<T> error(String message) {
        ResultDTO<T> resultDTO = new ResultDTO<>();
        resultDTO.setSign_type("md5");
        resultDTO.setSign("s1tTc4F8ZYzdJ7HkNUJkZw==");
        resultDTO.setCode(40004);
        resultDTO.setMsg("业务处理失败");
        resultDTO.setSub_code("request_thirdparty_service_return_error");
        resultDTO.setSub_msg("查询报表信息失败！");
        return resultDTO;
    }
}