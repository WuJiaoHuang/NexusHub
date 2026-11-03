package com.nexushub.service;

import com.nexushub.dto.EventRegistrationMessage;
import com.nexushub.service.impl.EventRegistrationCompensationServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventRegistrationCompensationTest {

    @Test
    void missingLuaResultMustFailInsteadOfAcknowledgingSuccessfulCompensation() {
        StringRedisTemplate redis = Mockito.mock(StringRedisTemplate.class);
        EventRegistrationCompensationServiceImpl service = new EventRegistrationCompensationServiceImpl();
        ReflectionTestUtils.setField(service, "stringRedisTemplate", redis);
        EventRegistrationMessage message = new EventRegistrationMessage(99L, 7L, 1L, LocalDateTime.now());

        assertThatThrownBy(() -> service.compensate(message)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void compensateShouldExecuteLuaWithStockOrderAndIdempotentKeys() {
        StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
        when(redisTemplate.execute(any(RedisScript.class), any(List.class), eq("event:slots:1"),
                eq("event:registration:1"), eq("event:registration:compensated:99"), eq("7"), eq("86400"))).thenReturn(0L);
        EventRegistrationCompensationServiceImpl service = new EventRegistrationCompensationServiceImpl();
        ReflectionTestUtils.setField(service, "stringRedisTemplate", redisTemplate);
        EventRegistrationMessage message = new EventRegistrationMessage(99L, 7L, 1L, LocalDateTime.now());

        Long result = service.compensate(message);

        assertThat(result).isZero();
        verify(redisTemplate).execute(any(RedisScript.class), any(List.class), eq("event:slots:1"),
                eq("event:registration:1"), eq("event:registration:compensated:99"), eq("7"), eq("86400"));
    }
}
