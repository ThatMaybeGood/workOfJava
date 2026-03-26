package com.mergedata.model.dto.Message;

import com.mergedata.model.vo.ApiResponseBodyList;
import com.mergedata.model.vo.ApiResponseResult;
import lombok.Data;

@Data
public class MsgResponse<T> {
    private MsgResponseResult result;
    private T body;



    // 构建成功响应 - 单对象
    public static <T> MsgResponse<T> success(String msg) {
        MsgResponse<T> response = new MsgResponse<>();
        response.setResult(MsgResponseResult.success(msg));
        return response;
    }
    // 构建失败响应 - 单对象
    public static <T> MsgResponse<T> failure(String msg) {
        MsgResponse<T> response = new MsgResponse<>();
        response.setResult(MsgResponseResult.failure(msg));
        return response;
    }


}
