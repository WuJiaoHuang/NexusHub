package com.cityeats.cache;

import com.cityeats.utils.CacheClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Component
public class MutexCacheStrategy implements CacheQueryStrategy {

    @Resource
    private CacheClient cacheClient;

    @Override
    public CacheStrategyType type() {
        return CacheStrategyType.MUTEX;
    }

    @Override
    public <R, ID> R query(String keyPrefix, ID id, Class<R> resultType,
                           Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        return cacheClient.queryWithMutex(keyPrefix, id, resultType, dbFallback, time, unit);
    }
}
