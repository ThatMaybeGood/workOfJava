package com.demo.integration.core.receive;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2026/5/9 23:12
 */
public interface ReceiveHandler {

    /**
     * 处理消息
     *
     * @param body 请求报文
     * @return 返回结果
     */
    String handle(String body);

    /**
     * handler名称
     */
    String getHandlerName();
}