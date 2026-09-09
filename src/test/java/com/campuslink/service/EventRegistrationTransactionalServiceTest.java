package com.campuslink.service;

import com.campuslink.common.BusinessException;
import com.campuslink.entity.EventRegistration;
import com.campuslink.mapper.EventCapacityMapper;
import com.campuslink.mapper.EventRegistrationMapper;
import com.campuslink.service.impl.EventRegistrationTransactionalServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventRegistrationTransactionalServiceTest {

    @Test
    void createEventRegistrationShouldInsertBeforeConditionalSlotsUpdate() {
        EventRegistrationMapper registrationMapper = Mockito.mock(EventRegistrationMapper.class);
        EventCapacityMapper capacityMapper = Mockito.mock(EventCapacityMapper.class);
        when(registrationMapper.selectCount(any())).thenReturn(0);
        when(capacityMapper.update(isNull(), any())).thenReturn(1);

        EventRegistrationTransactionalServiceImpl service = new EventRegistrationTransactionalServiceImpl();
        ReflectionTestUtils.setField(service, "eventRegistrationMapper", registrationMapper);
        ReflectionTestUtils.setField(service, "eventCapacityMapper", capacityMapper);

        EventRegistration registration = registration(1L, 1L, 1L);
        service.createEventRegistration(registration);

        InOrder inOrder = Mockito.inOrder(registrationMapper, capacityMapper);
        inOrder.verify(registrationMapper).insert(registration);
        inOrder.verify(capacityMapper).update(isNull(), any());
    }

    @Test
    void createEventRegistrationShouldThrowWhenConditionalSlotsUpdateFails() {
        EventRegistrationMapper registrationMapper = Mockito.mock(EventRegistrationMapper.class);
        EventCapacityMapper capacityMapper = Mockito.mock(EventCapacityMapper.class);
        when(registrationMapper.selectCount(any())).thenReturn(0);
        when(capacityMapper.update(isNull(), any())).thenReturn(0);

        EventRegistrationTransactionalServiceImpl service = new EventRegistrationTransactionalServiceImpl();
        ReflectionTestUtils.setField(service, "eventRegistrationMapper", registrationMapper);
        ReflectionTestUtils.setField(service, "eventCapacityMapper", capacityMapper);

        assertThrows(BusinessException.class, () -> service.createEventRegistration(registration(1L, 1L, 1L)));
        verify(registrationMapper).insert(any());
        verify(capacityMapper).update(isNull(), any());
    }

    @Test
    void createEventRegistrationShouldNotConsumeSlotsWhenUniqueIndexConflictHappens() {
        EventRegistrationMapper registrationMapper = Mockito.mock(EventRegistrationMapper.class);
        EventCapacityMapper capacityMapper = Mockito.mock(EventCapacityMapper.class);
        when(registrationMapper.selectCount(any())).thenReturn(0);
        when(registrationMapper.insert(any())).thenThrow(new DuplicateKeyException("duplicate event registration"));

        EventRegistrationTransactionalServiceImpl service = new EventRegistrationTransactionalServiceImpl();
        ReflectionTestUtils.setField(service, "eventRegistrationMapper", registrationMapper);
        ReflectionTestUtils.setField(service, "eventCapacityMapper", capacityMapper);

        assertThrows(DuplicateKeyException.class, () -> service.createEventRegistration(registration(1L, 1L, 1L)));
        verify(capacityMapper, never()).update(isNull(), any());
    }

    private EventRegistration registration(Long id, Long userId, Long eventId) {
        EventRegistration registration = new EventRegistration();
        registration.setId(id);
        registration.setUserId(userId);
        registration.setEventId(eventId);
        return registration;
    }
}
