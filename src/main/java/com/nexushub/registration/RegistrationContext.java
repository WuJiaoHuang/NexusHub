package com.nexushub.registration;

import com.nexushub.entity.EventCapacity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RegistrationContext {
    private Long eventId;
    private Long userId;
    private LocalDateTime now;
    private EventCapacity eventCapacity;
}
