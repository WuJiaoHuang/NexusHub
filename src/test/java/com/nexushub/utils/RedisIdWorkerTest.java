package com.nexushub.utils;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class RedisIdWorkerTest {

    @Test
    void nextIdShouldBeUniqueUnderConcurrency() throws InterruptedException {
        StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
        ValueOperations<String, String> operations = Mockito.mock(ValueOperations.class);
        AtomicLong counter = new AtomicLong();
        when(redisTemplate.opsForValue()).thenReturn(operations);
        when(operations.increment(anyString())).thenAnswer(invocation -> counter.incrementAndGet());

        RedisIdWorker worker = new RedisIdWorker();
        ReflectionTestUtils.setField(worker, "stringRedisTemplate", redisTemplate);

        int count = 1000;
        Set<Long> ids = Collections.newSetFromMap(new ConcurrentHashMap<>());
        CountDownLatch latch = new CountDownLatch(count);
        ExecutorService executor = Executors.newFixedThreadPool(16);
        for (int i = 0; i < count; i++) {
            executor.submit(() -> {
                ids.add(worker.nextId("order"));
                latch.countDown();
            });
        }
        latch.await();
        executor.shutdown();
        assertThat(ids).hasSize(count);
    }
}
