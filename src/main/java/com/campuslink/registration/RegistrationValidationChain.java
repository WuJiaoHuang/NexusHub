package com.campuslink.registration;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RegistrationValidationChain {

    private final List<RegistrationValidator> validators;

    public RegistrationValidationChain(List<RegistrationValidator> validators) {
        this.validators = validators;
    }

    public void validate(RegistrationContext context) {
        for (RegistrationValidator validator : validators) {
            validator.validate(context);
        }
    }
}
