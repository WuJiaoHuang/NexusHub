package com.campuslink.registration;

import com.campuslink.common.BusinessException;
import com.campuslink.entity.EventCapacity;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(20)
@Component
public class RegistrationTimeValidator implements RegistrationValidator {
    @Override
    public void validate(RegistrationContext context) {
        EventCapacity event = context.getEventCapacity();
        if (event.getRegistrationStartTime().isAfter(context.getNow())) {
            throw new BusinessException("热门报名尚未开始");
        }
        if (event.getRegistrationEndTime().isBefore(context.getNow())) {
            throw new BusinessException("热门报名已经结束");
        }
    }
}

