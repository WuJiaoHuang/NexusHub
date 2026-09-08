package com.campuslink.registration;

import com.campuslink.common.BusinessException;
import com.campuslink.entity.EventCapacity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegistrationValidationChainTest {

    @Test
    void chainShouldStopWhenCampusEventMissing() {
        RegistrationContext context = new RegistrationContext();
        context.setEventId(1L);
        context.setNow(LocalDateTime.now());
        RegistrationValidationChain chain = new RegistrationValidationChain(Arrays.asList(
                new EventExistValidator(),
                new RegistrationTimeValidator()));

        assertThatThrownBy(() -> chain.validate(context))
                .isInstanceOf(BusinessException.class)
                .hasMessage("校园活动不存在");
    }

    @Test
    void chainShouldRejectNotStartedCampusEvent() {
        EventCapacity event = new EventCapacity();
        event.setRegistrationStartTime(LocalDateTime.now().plusMinutes(10));
        event.setRegistrationEndTime(LocalDateTime.now().plusHours(1));
        RegistrationContext context = new RegistrationContext();
        context.setEventId(1L);
        context.setNow(LocalDateTime.now());
        context.setEventCapacity(event);
        RegistrationValidationChain chain = new RegistrationValidationChain(Arrays.asList(
                new EventExistValidator(),
                new RegistrationTimeValidator()));

        assertThatThrownBy(() -> chain.validate(context))
                .isInstanceOf(BusinessException.class)
                .hasMessage("热门报名尚未开始");
    }
}

