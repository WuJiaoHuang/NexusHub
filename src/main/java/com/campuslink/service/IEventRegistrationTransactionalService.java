package com.campuslink.service;

import com.campuslink.entity.EventRegistration;

public interface IEventRegistrationTransactionalService {
    void createEventRegistration(EventRegistration eventRegistration);
}
