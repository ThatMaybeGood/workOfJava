package com.demo.integration.core.receive.impl;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2026/5/9 23:12
 */


import com.demo.integration.core.executor.PostExecutor;
import com.demo.integration.core.receive.ReceiveHandler;
import com.demo.integration.strategy.MessageTransformStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class SpdReceiveHandler implements ReceiveHandler {

    @Resource
    private MessageTransformStrategy spdTransformStrategy;

    @Resource
    private PostExecutor postExecutor;

    @Override
    public String handle(String body) {

        log.info("SPD消息开始处理");

        // 1. 报文转换
        Object requestDto =
                spdTransformStrategy.transform(body);

        // 2. 转发
        String result =
                postExecutor.execute(
                        "http://localhost:8081/spd",
                        requestDto
                );

        log.info("SPD消息处理完成");

        return result;
    }

    @Override
    public String getHandlerName() {
        return "SPD";
    }
}