package com.campuslink.cache;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class CacheStrategyFactory {

    private final Map<CacheStrategyType, CacheQueryStrategy> strategies = new EnumMap<>(CacheStrategyType.class);

    public CacheStrategyFactory(List<CacheQueryStrategy> strategyList) {
        for (CacheQueryStrategy strategy : strategyList) {
            strategies.put(strategy.type(), strategy);
        }
    }

    public CacheQueryStrategy get(CacheStrategyType type) {
        CacheQueryStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("未知缓存策略: " + type);
        }
        return strategy;
    }
}
