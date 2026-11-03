package com.nexushub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventRegistrationMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long registrationId;
    private Long userId;
    private Long eventId;
    private LocalDateTime createTime;
}

