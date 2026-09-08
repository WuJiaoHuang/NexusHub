package com.campuslink.registration;

import com.campuslink.entity.EventCapacity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RegistrationContext {
    private Long eventId;
    private Long userId;
    private LocalDateTime now;
    private EventCapacity eventCapacity;
}
