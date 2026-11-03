package com.nexushub.service;

import com.nexushub.entity.EventRegistration;

public interface IEventRegistrationTransactionalService {
    void createEventRegistration(EventRegistration eventRegistration);
}
