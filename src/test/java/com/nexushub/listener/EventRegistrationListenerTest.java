package com.nexushub.listener;

import com.nexushub.config.QueueConfig;
import com.nexushub.dto.EventRegistrationMessage;
import com.nexushub.entity.EventRegistration;
import com.nexushub.mapper.EventRegistrationMapper;
import com.nexushub.service.EventRegistrationCompensationService;
import com.nexushub.service.IEventRegistrationTransactionalService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EventRegistrationListenerTest {
    private final EventRegistrationMessage message = new EventRegistrationMessage(
            99L, 7L, 1L, LocalDateTime.of(2026, 11, 3, 12, 0));

    @Test
    void consumerPreservesMessageFieldsAndPropagatesFailuresForRetry() {
        IEventRegistrationTransactionalService service = mock(IEventRegistrationTransactionalService.class);
        EventRegistrationListener listener = new EventRegistrationListener();
        ReflectionTestUtils.setField(listener, "transactionalService", service);
        IllegalStateException failure = new IllegalStateException("database unavailable");
        doThrow(failure).when(service).createEventRegistration(any());

        assertThatThrownBy(() -> listener.handleEventRegistration(message)).isSameAs(failure);
        ArgumentCaptor<EventRegistration> record = ArgumentCaptor.forClass(EventRegistration.class);
        verify(service).createEventRegistration(record.capture());
        assertThat(record.getValue().getId()).isEqualTo(99L);
        assertThat(record.getValue().getUserId()).isEqualTo(7L);
        assertThat(record.getValue().getEventId()).isEqualTo(1L);
        assertThat(record.getValue().getCreateTime()).isEqualTo(message.getCreateTime());
    }

    @Test
    void deadLetterCompensatesOnlyWhenRegistrationWasNotCommitted() {
        EventRegistrationMapper mapper = mock(EventRegistrationMapper.class);
        EventRegistrationCompensationService compensation = mock(EventRegistrationCompensationService.class);
        EventRegistrationDeadLetterListener listener = new EventRegistrationDeadLetterListener();
        ReflectionTestUtils.setField(listener, "eventRegistrationMapper", mapper);
        ReflectionTestUtils.setField(listener, "compensationService", compensation);
        when(mapper.selectById(99L)).thenReturn(new EventRegistration());
        listener.handleDeadLetter(message);
        verifyNoInteractions(compensation);

        when(mapper.selectById(99L)).thenReturn(null);
        listener.handleDeadLetter(message);
        verify(compensation).compensate(message);
    }

    @Test
    void concreteListenerTypeCanReadQueuedMessageWithLegacyPackageHeader() {
        MessageConverter converter = new QueueConfig().messageConverter();
        Message encoded = converter.toMessage(message, new MessageProperties());
        // Retain this old wire header solely to verify messages created before the package migration.
        encoded.getMessageProperties().setHeader("__TypeId__", "com.campuslink.dto.EventRegistrationMessage");
        encoded.getMessageProperties().setInferredArgumentType(EventRegistrationMessage.class);

        Object decoded = converter.fromMessage(encoded);
        assertThat(decoded).isInstanceOf(EventRegistrationMessage.class);
        assertThat(decoded).isEqualTo(message);
    }

    @Test
    void legacyHeaderCanBeReadWithoutInferredListenerTypeAndNewMessagesUseNewPackage() {
        MessageConverter converter = new QueueConfig().messageConverter();
        Message encoded = converter.toMessage(message, new MessageProperties());
        assertThat(encoded.getMessageProperties().getHeaders().get("__TypeId__"))
                .isEqualTo("com.nexushub.dto.EventRegistrationMessage");
        encoded.getMessageProperties().setHeader("__TypeId__", "com.campuslink.dto.EventRegistrationMessage");
        assertThat(converter.fromMessage(encoded)).isEqualTo(message);
    }

    @Test
    void unknownLegacyTypesAreNotTrustedForDeserialization() {
        MessageConverter converter = new QueueConfig().messageConverter();
        Message encoded = converter.toMessage(message, new MessageProperties());
        encoded.getMessageProperties().setHeader("__TypeId__", "com.campuslink.entity.CampusEvent");
        assertThatThrownBy(() -> converter.fromMessage(encoded)).isInstanceOf(IllegalArgumentException.class);
    }
}
