package com.campuslink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campuslink.dto.Result;
import com.campuslink.entity.EventRegistration;

public interface IEventRegistrationService extends IService<EventRegistration> {
    Result registerForEvent(Long eventId);
    Result queryMyRegistrationStatus(Long eventId);
    Result queryMyRegistrations();
}
