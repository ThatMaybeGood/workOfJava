package com.demo.integration.strategy;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2026/5/9 23:30
 */

public interface MessageTransformStrategy {

    /**
     * 报文转换
     */
    Object transform(String body);
}