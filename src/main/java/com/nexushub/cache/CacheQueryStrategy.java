package com.nexushub.cache;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public interface CacheQueryStrategy {
    CacheStrategyType type();

    <R, ID> R query(String keyPrefix, ID id, Class<R> resultType,
                    Function<ID, R> dbFallback, Long time, TimeUnit unit);
}
