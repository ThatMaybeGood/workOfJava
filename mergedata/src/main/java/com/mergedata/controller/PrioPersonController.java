package com.mergedata.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mergedata.model.dto.Message.MsgRequest;
import com.mergedata.model.dto.Message.MsgRequestBody;
import com.mergedata.model.dto.Message.MsgRequestData;
import com.mergedata.model.dto.Message.MsgResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/message/rcv")
public class PrioPersonController {
    ObjectMapper mapper = new ObjectMapper();

    @PostMapping("/prioPersonMessage")
    public MsgResponse<?> prioPersonMessage(@RequestBody MsgRequest request,
                                            HttpServletRequest httpRequest) {

//        log.info("========== 接收到请求 ==========");
//        log.info("请求URL: {}", httpRequest.getRequestURL());
//        log.info("请求方法: {}", httpRequest.getMethod());
//
//        // 打印请求头
//        log.info("========== 请求头 ==========");
//        Enumeration<String> headerNames = httpRequest.getHeaderNames();
//        while (headerNames.hasMoreElements()) {
//            String headerName = headerNames.nextElement();
//            log.info("{}: {}", headerName, httpRequest.getHeader(headerName));
//        }

        try {
            log.info(mapper.writeValueAsString(request));

            if (request != null && request.getBody() != null) {
                MsgRequestBody body = request.getBody();

                // 解析 data 字符串
                MsgRequestData data = body.getDataObject();
                if (data == null) {
                    return MsgResponse.failure("data 解析失败或为空");
                }
            } else {
                return MsgResponse.failure("请求体为空");
            }

        } catch (Exception e) {
            //失败情况  把原始请求体打印  方便调试
            log.info(request.toString());
            log.error("接收消息失败: {}", e.getMessage(), e);
            return MsgResponse.failure("接收消息失败: " + e.getMessage());
        }
        log.info("========== 接收消息完成，返回成功响应 ==========");
        return MsgResponse.success("操作成功");
    }

}