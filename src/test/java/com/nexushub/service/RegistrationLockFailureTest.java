package com.nexushub.service;

import com.nexushub.dto.EventRegistrationMessage;
import com.nexushub.listener.EventRegistrationListener;
import com.nexushub.mapper.EventCapacityMapper;
import com.nexushub.mapper.EventRegistrationMapper;
import com.nexushub.service.impl.EventRegistrationTransactionalServiceImpl;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

class RegistrationLockFailureTest {
    private final RLock lock = mock(RLock.class);
    private final RedissonClient redisson = mock(RedissonClient.class);
    private final EventRegistrationMapper registrations = mock(EventRegistrationMapper.class);
    private final EventCapacityMapper capacities = mock(EventCapacityMapper.class);

    private EventRegistrationListener listener() {
        when(redisson.getLock("lock:event:registration:1:7")).thenReturn(lock);
        EventRegistrationTransactionalServiceImpl service = new EventRegistrationTransactionalServiceImpl();
        ReflectionTestUtils.setField(service, "redissonClient", redisson);
        ReflectionTestUtils.setField(service, "eventRegistrationMapper", registrations);
        ReflectionTestUtils.setField(service, "eventCapacityMapper", capacities);
        EventRegistrationListener listener = new EventRegistrationListener();
        ReflectionTestUtils.setField(listener, "transactionalService", service);
        return listener;
    }

    private EventRegistrationMessage message() {
        return new EventRegistrationMessage(99L, 7L, 1L, LocalDateTime.now());
    }

    @Test
    void busyLockPropagatesFailureOnEveryAttemptInsteadOfAcknowledgingAnUnwrittenMessage() throws Exception {
        when(lock.tryLock(1, 5, TimeUnit.SECONDS)).thenReturn(false);
        EventRegistrationListener listener = listener();
        for (int attempt = 0; attempt < 3; attempt++) {
            assertThatThrownBy(() -> listener.handleEventRegistration(message()))
                    .isInstanceOf(IllegalStateException.class).hasMessageContaining("等待消息重试");
        }
        verifyNoInteractions(registrations, capacities);
        verify(lock, never()).unlock();
    }

    @Test
    void interruptedLockPreservesInterruptStatusAndPropagatesFailureToConsumer() throws Exception {
        when(lock.tryLock(1, 5, TimeUnit.SECONDS)).thenThrow(new InterruptedException("cancelled"));
        EventRegistrationListener listener = listener();
        try {
            assertThatThrownBy(() -> listener.handleEventRegistration(message()))
                    .isInstanceOf(IllegalStateException.class).hasCauseInstanceOf(InterruptedException.class);
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
            verifyNoInteractions(registrations, capacities);
            verify(lock, never()).unlock();
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void acquiredLockStillInsertsAndConditionallyDecrementsThenUnlocks() throws Exception {
        when(lock.tryLock(1, 5, TimeUnit.SECONDS)).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);
        when(registrations.selectCount(any())).thenReturn(0);
        when(capacities.update(isNull(), any())).thenReturn(1);

        listener().handleEventRegistration(message());

        verify(registrations).insert(any());
        verify(capacities).update(isNull(), any());
        verify(lock).unlock();
    }
}
