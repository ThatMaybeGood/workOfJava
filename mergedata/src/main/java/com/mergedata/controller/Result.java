package com.mergedata.controller;

import lombok.Data;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2025/10/30 11:01
 */ // 统一返回结果
@Data
class Result<T> {
    private String sign_type;
    private String sign;
    private int code;
    private String msg;
    private String sub_code;
    private String sub_msg;
    private T list;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setSign_type("md5");
        result.setSign("s1tTc4F8ZYzdJ7HkNUJkZw==");
        result.setCode(10000);
        result.setMsg("接口调用成功，并且业务系统也处理成功");
        result.setSub_code("success");
        result.setSub_msg("查询报表信息成功！");
        result.setList(data);
        return result;
    }

    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setSign_type("md5");
        result.setSign("s1tTc4F8ZYzdJ7HkNUJkZw==");
        result.setCode(40004);
        result.setMsg("业务处理失败");
        result.setSub_code("request_thirdparty_service_return_error");
        result.setSub_msg("查询报表信息失败！");
        return result;
    }
}