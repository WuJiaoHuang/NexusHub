package com.nexushub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nexushub.dto.Result;
import com.nexushub.entity.EventRegistration;

public interface IEventRegistrationService extends IService<EventRegistration> {
    Result registerForEvent(Long eventId);
    Result queryMyRegistrationStatus(Long eventId);
    Result queryMyRegistrations();
}
