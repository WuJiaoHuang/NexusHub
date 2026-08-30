package com.cityeats.utils;

import com.cityeats.entity.Shop;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CacheClientTest {

    @Test
    void queryWithPassThroughShouldCacheNullValue() {
        StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
        ValueOperations<String, String> operations = Mockito.mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(operations);
        when(operations.get("cache:shop:99")).thenReturn(null);

        CacheClient cacheClient = new CacheClient();
        ReflectionTestUtils.setField(cacheClient, "stringRedisTemplate", redisTemplate);

        Shop result = cacheClient.queryWithPassThrough("cache:shop:", 99L, Shop.class, id -> null, 30L, TimeUnit.MINUTES);

        assertThat(result).isNull();
        verify(operations).set(eq("cache:shop:99"), eq(""), eq(RedisConstants.CACHE_NULL_TTL), eq(TimeUnit.MINUTES));
    }
}
