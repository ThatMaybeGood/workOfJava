package com.demo.integration.controller;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2026/5/9 23:33
 */
import com.demo.integration.core.receive.ReceiveHandler;
import com.demo.integration.core.receive.ReceiveHandlerRegistry;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/receive")
public class MessageReceiveController {

    @Resource
    private ReceiveHandlerRegistry registry;

    /**
     * 动态接收
     */
    @PostMapping("/{type}")
    public String receive(
            @PathVariable("type") String type,
            @RequestBody String body,
            HttpServletRequest request
    ) {

        // 根据type获取handler
        ReceiveHandler handler =
                registry.getHandler(type.toUpperCase());

        // 执行处理
        return handler.handle(body);
    }
}
