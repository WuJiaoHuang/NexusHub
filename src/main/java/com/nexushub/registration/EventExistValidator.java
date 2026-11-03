package com.nexushub.registration;

import com.nexushub.common.BusinessException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(10)
@Component
public class EventExistValidator implements RegistrationValidator {
    @Override
    public void validate(RegistrationContext context) {
        if (context.getEventId() == null) {
            throw new BusinessException("活动 id 不能为空");
        }
        if (context.getEventCapacity() == null) {
            throw new BusinessException("活动不存在");
        }
    }
}
