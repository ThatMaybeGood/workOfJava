package com.messageTransformer.strategy;

public interface MessageStrategy {

    /**
     * 转换消息
     */
    Object transform(String body);
}