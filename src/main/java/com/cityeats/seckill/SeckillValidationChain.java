package com.cityeats.seckill;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SeckillValidationChain {

    private final List<SeckillValidator> validators;

    public SeckillValidationChain(List<SeckillValidator> validators) {
        this.validators = validators;
    }

    public void validate(SeckillContext context) {
        for (SeckillValidator validator : validators) {
            validator.validate(context);
        }
    }
}
