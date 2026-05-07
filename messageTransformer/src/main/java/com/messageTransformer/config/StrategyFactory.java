package com.messageTransformer.config;

import com.messageTransformer.strategy.MessageStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class StrategyFactory {

    @Autowired
    private Map<String, MessageStrategy> strategyMap;

    public MessageStrategy getStrategy(String name) {
        return strategyMap.get(name);
    }
}