package com.campuslink.service;

import com.campuslink.common.BusinessException;
import com.campuslink.entity.EventRegistration;
import com.campuslink.mapper.CampusEventMapper;
import com.campuslink.mapper.EventCapacityMapper;
import com.campuslink.mapper.EventRegistrationMapper;
import com.campuslink.service.impl.EventRegistrationTransactionalServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventRegistrationTransactionalServiceTest {

    @Test
    void createEventRegistrationShouldUseConditionalStockUpdateBeforeInsert() {
        EventRegistrationMapper registrationMapper = Mockito.mock(EventRegistrationMapper.class);
        EventCapacityMapper capacityMapper = Mockito.mock(EventCapacityMapper.class);
        CampusEventMapper campusEventMapper = Mockito.mock(CampusEventMapper.class);
        when(registrationMapper.selectCount(any())).thenReturn(0);
        when(capacityMapper.update(isNull(), any())).thenReturn(1);

        EventRegistrationTransactionalServiceImpl service = new EventRegistrationTransactionalServiceImpl();
        ReflectionTestUtils.setField(service, "eventRegistrationMapper", registrationMapper);
        ReflectionTestUtils.setField(service, "eventCapacityMapper", capacityMapper);
        ReflectionTestUtils.setField(service, "campusEventMapper", campusEventMapper);

        EventRegistration registration = new EventRegistration();
        registration.setId(1L);
        registration.setUserId(1L);
        registration.setEventId(1L);
        service.createEventRegistration(registration);

        verify(capacityMapper).update(isNull(), any());
        verify(registrationMapper).insert(registration);
    }

    @Test
    void createEventRegistrationShouldThrowWhenConditionalStockUpdateFails() {
        EventRegistrationMapper registrationMapper = Mockito.mock(EventRegistrationMapper.class);
        EventCapacityMapper capacityMapper = Mockito.mock(EventCapacityMapper.class);
        CampusEventMapper campusEventMapper = Mockito.mock(CampusEventMapper.class);
        when(registrationMapper.selectCount(any())).thenReturn(0);
        when(capacityMapper.update(isNull(), any())).thenReturn(0);

        EventRegistrationTransactionalServiceImpl service = new EventRegistrationTransactionalServiceImpl();
        ReflectionTestUtils.setField(service, "eventRegistrationMapper", registrationMapper);
        ReflectionTestUtils.setField(service, "eventCapacityMapper", capacityMapper);
        ReflectionTestUtils.setField(service, "campusEventMapper", campusEventMapper);

        EventRegistration registration = new EventRegistration();
        registration.setId(1L);
        registration.setUserId(1L);
        registration.setEventId(1L);

        assertThrows(BusinessException.class, () -> service.createEventRegistration(registration));
        verify(capacityMapper).update(isNull(), any());
    }
}

