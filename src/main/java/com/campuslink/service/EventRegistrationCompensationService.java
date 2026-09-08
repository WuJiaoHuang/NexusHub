package com.campuslink.service;

import com.campuslink.dto.EventRegistrationMessage;

public interface EventRegistrationCompensationService {
    Long compensate(EventRegistrationMessage message);
}
