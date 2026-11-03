package com.nexushub.service;

import com.nexushub.dto.EventRegistrationMessage;

public interface EventRegistrationCompensationService {
    Long compensate(EventRegistrationMessage message);
}
