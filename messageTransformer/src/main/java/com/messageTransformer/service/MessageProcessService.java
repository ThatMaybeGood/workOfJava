package com.messageTransformer.service;

import com.messageTransformer.config.RouteConfig;
import com.messageTransformer.config.StrategyFactory;
import com.messageTransformer.strategy.MessageStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageProcessService {

    @Autowired
    private RouteConfig routeConfig;

    @Autowired
    private StrategyFactory strategyFactory;

    @Autowired
    private ForwardService forwardService;

    public String process(String path, String body) {

        RouteConfig.RouteInfo route = routeConfig.getRoutes().get(path);

        if (route == null) {
            throw new RuntimeException("未配置路由: " + path);
        }

        // 1. 获取策略
        MessageStrategy strategy = strategyFactory.getStrategy(route.getStrategy());

        // 2. 转换报文
        Object transformed = strategy.transform(body);

        // 3. 转发
        return forwardService.post(route.getTargetUrl(), transformed);
    }
}