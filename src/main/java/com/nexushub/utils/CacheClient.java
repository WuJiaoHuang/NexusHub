package com.nexushub.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
@Component
public class CacheClient {

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(4);

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public void set(String key, Object value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }

    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    public <R, ID> R queryWithPassThrough(String keyPrefix, ID id, Class<R> type,
                                          Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(json)) {
            return JSONUtil.toBean(json, type);
        }
        if (json != null) {
            return null;
        }
        R result = dbFallback.apply(id);
        if (result == null) {
            stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
        set(key, result, time, unit);
        return result;
    }

    public <R, ID> R queryWithMutex(String keyPrefix, ID id, Class<R> type,
                                    Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(json)) {
            return JSONUtil.toBean(json, type);
        }
        if (json != null) {
            return null;
        }
        String lockKey = RedisConstants.LOCK_PLACE_KEY + id;
        R result = null;
        try {
            boolean locked = tryLock(lockKey);
            if (!locked) {
                Thread.sleep(50);
                return queryWithMutex(keyPrefix, id, type, dbFallback, time, unit);
            }
            result = dbFallback.apply(id);
            if (result == null) {
                stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
                return null;
            }
            set(key, result, time, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("缓存重建被中断，key={}", key);
        } finally {
            unlock(lockKey);
        }
        return result;
    }

    public <R, ID> R queryWithLogicalExpire(String keyPrefix, ID id, Class<R> type,
                                            Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isBlank(json)) {
            return null;
        }
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        R result = JSONUtil.toBean(JSONUtil.toJsonStr(redisData.getData()), type);
        if (redisData.getExpireTime().isAfter(LocalDateTime.now())) {
            return result;
        }
        String lockKey = RedisConstants.LOCK_PLACE_KEY + id;
        if (tryLock(lockKey)) {
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    R fresh = dbFallback.apply(id);
                    setWithLogicalExpire(key, fresh, time, unit);
                } catch (Exception e) {
                    log.warn("逻辑过期缓存重建失败，key={}", key, e);
                } finally {
                    unlock(lockKey);
                }
            });
        }
        return result;
    }

    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", RedisConstants.LOCK_SHOP_TTL, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }
}
