package com.cityeats.service;

import com.cityeats.dto.VoucherOrderMessage;
import com.cityeats.service.impl.VoucherOrderCompensationServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VoucherOrderCompensationTest {

    @Test
    void compensateShouldExecuteLuaWithStockOrderAndIdempotentKeys() {
        StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
        when(redisTemplate.execute(any(RedisScript.class), any(List.class), eq("seckill:stock:1"),
                eq("seckill:order:1"), eq("seckill:compensated:99"), eq("7"), eq("86400"))).thenReturn(0L);
        VoucherOrderCompensationServiceImpl service = new VoucherOrderCompensationServiceImpl();
        ReflectionTestUtils.setField(service, "stringRedisTemplate", redisTemplate);
        VoucherOrderMessage message = new VoucherOrderMessage(99L, 7L, 1L, LocalDateTime.now());

        Long result = service.compensate(message);

        assertThat(result).isZero();
        verify(redisTemplate).execute(any(RedisScript.class), any(List.class), eq("seckill:stock:1"),
                eq("seckill:order:1"), eq("seckill:compensated:99"), eq("7"), eq("86400"));
    }
}
