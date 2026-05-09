package com.demo.integration.core.receive;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2026/5/9 23:12
 */

import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ReceiveHandlerRegistry {

    /**
     * handler容器
     */
    private final Map<String, ReceiveHandler> HANDLER_MAP = new HashMap<>();

    @Resource
    public void init(List<ReceiveHandler> handlers) {

        for (ReceiveHandler handler : handlers) {

            HANDLER_MAP.put(
                    handler.getHandlerName(),
                    handler
            );
        }
    }

    /**
     * 获取handler
     */
    public ReceiveHandler getHandler(String name) {

        ReceiveHandler handler = HANDLER_MAP.get(name);

        if (handler == null) {
            throw new RuntimeException("未找到handler: " + name);
        }

        return handler;
    }
}
