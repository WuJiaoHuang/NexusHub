package com.nexushub.service;

import com.nexushub.common.MqConstants;
import com.nexushub.dto.EventRegistrationMessage;
import com.nexushub.dto.Result;
import com.nexushub.dto.UserDTO;
import com.nexushub.entity.EventCapacity;
import com.nexushub.registration.RegistrationValidationChain;
import com.nexushub.service.impl.EventRegistrationServiceImpl;
import com.nexushub.utils.RedisIdWorker;
import com.nexushub.utils.UserHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpIOException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EventRegistrationPublishTest {
    private final RabbitTemplate rabbit = mock(RabbitTemplate.class);
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    private final EventRegistrationCompensationService compensation = mock(EventRegistrationCompensationService.class);
    private final EventRegistrationServiceImpl service = new EventRegistrationServiceImpl();

    @BeforeEach
    void setUp() {
        IEventCapacityService capacities = mock(IEventCapacityService.class);
        when(capacities.getById(1L)).thenReturn(new EventCapacity());
        RedisIdWorker ids = mock(RedisIdWorker.class);
        when(ids.nextId("event_registration")).thenReturn(99L);
        when(redis.execute(any(RedisScript.class), eq(Collections.emptyList()), eq("1"), eq("7"))).thenReturn(0L);
        when(compensation.compensate(any())).thenReturn(0L);
        ReflectionTestUtils.setField(service, "eventCapacityService", capacities);
        ReflectionTestUtils.setField(service, "redisIdWorker", ids);
        ReflectionTestUtils.setField(service, "stringRedisTemplate", redis);
        ReflectionTestUtils.setField(service, "rabbitTemplate", rabbit);
        ReflectionTestUtils.setField(service, "registrationValidationChain", mock(RegistrationValidationChain.class));
        ReflectionTestUtils.setField(service, "compensationService", compensation);
        ReflectionTestUtils.setField(service, "publishConfirmTimeoutMs", 1L);
        UserDTO user = new UserDTO();
        user.setId(7L);
        UserHolder.saveUser(user);
    }

    @AfterEach
    void clearUser() {
        UserHolder.removeUser();
        Thread.interrupted();
    }

    private void confirm(boolean ack, String reason, boolean returned) {
        doAnswer(invocation -> {
            CorrelationData data = invocation.getArgument(3);
            assertThat(data.getId()).isEqualTo("99");
            if (returned) {
                data.setReturnedMessage(new Message(new byte[0], new MessageProperties()));
            }
            data.getFuture().set(new CorrelationData.Confirm(ack, reason));
            return null;
        }).when(rabbit).convertAndSend(eq(MqConstants.EVENT_REGISTRATION_EXCHANGE),
                eq(MqConstants.EVENT_REGISTRATION_ROUTING_KEY), any(EventRegistrationMessage.class), any(CorrelationData.class));
    }

    @Test
    void brokerAckWithoutReturnAcceptsRegistrationWithoutCompensation() {
        confirm(true, null, false);
        Result result = service.registerForEvent(1L);
        assertThat(result.getSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo(99L);
        verifyNoInteractions(compensation);
    }

    @Test
    void unroutableReturnCompensatesEvenWhenBrokerAcknowledgesPublish() {
        confirm(true, null, true);
        assertThat(service.registerForEvent(1L).getSuccess()).isFalse();
        verify(compensation).compensate(argThat(message -> message.getRegistrationId() == 99L
                && message.getUserId() == 7L && message.getEventId() == 1L));
    }

    @Test
    void brokerNackCompensatesRejectedPublish() {
        confirm(false, null, false);
        assertThat(service.registerForEvent(1L).getSuccess()).isFalse();
        verify(compensation).compensate(any());
    }

    @Test
    void connectionFailureBeforeSendCompensatesReservation() {
        doThrow(new AmqpConnectException(new IOException("connection refused"))).when(rabbit)
                .convertAndSend(anyString(), anyString(), any(EventRegistrationMessage.class), any(CorrelationData.class));
        assertThat(service.registerForEvent(1L).getErrorMsg()).contains("名额已恢复");
        verify(compensation).compensate(any());
    }

    @Test
    void conversionFailureBeforeSendCompensatesReservation() {
        doThrow(new MessageConversionException("cannot serialize")).when(rabbit)
                .convertAndSend(anyString(), anyString(), any(EventRegistrationMessage.class), any(CorrelationData.class));
        assertThat(service.registerForEvent(1L).getSuccess()).isFalse();
        verify(compensation).compensate(any());
    }

    @Test
    void confirmationTimeoutRetainsReservationForReconciliation() {
        assertThat(service.registerForEvent(1L).getErrorMsg()).contains("待确认");
        verifyNoInteractions(compensation);
    }

    @Test
    void networkIoFailureDoesNotBlindlyRestorePotentiallyDeliveredMessage() {
        doThrow(new AmqpIOException(new IOException("connection reset after write"))).when(rabbit)
                .convertAndSend(anyString(), anyString(), any(EventRegistrationMessage.class), any(CorrelationData.class));
        assertThat(service.registerForEvent(1L).getErrorMsg()).contains("待确认");
        verifyNoInteractions(compensation);
    }

    @Test
    void frameworkNackFromClosedChannelRetainsUncertainReservation() {
        confirm(false, "Channel closed by application", false);
        assertThat(service.registerForEvent(1L).getErrorMsg()).contains("待确认");
        verifyNoInteractions(compensation);
    }

    @Test
    void failedCompensationIsReportedInsteadOfClaimingRestoredCapacity() {
        confirm(true, null, true);
        when(compensation.compensate(any())).thenThrow(new IllegalStateException("redis unavailable"));
        assertThat(service.registerForEvent(1L).getErrorMsg()).contains("名额恢复待处理");
    }

    @Test
    void luaRejectionNeverPublishesOrCompensates() {
        when(redis.execute(any(RedisScript.class), eq(Collections.emptyList()), eq("1"), eq("7"))).thenReturn(1L);
        assertThat(service.registerForEvent(1L).getSuccess()).isFalse();
        verifyNoInteractions(rabbit, compensation);
    }
}
