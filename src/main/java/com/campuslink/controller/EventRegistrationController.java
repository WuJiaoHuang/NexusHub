package com.campuslink.controller;

import com.campuslink.dto.Result;
import com.campuslink.service.IEventRegistrationService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/event-registrations")
public class EventRegistrationController {
    @Resource
    private IEventRegistrationService eventRegistrationService;

    @PostMapping("/{eventId}")
    public Result register(@PathVariable("eventId") Long eventId) {
        return eventRegistrationService.registerForEvent(eventId);
    }

    @GetMapping("/me")
    public Result myRegistrations() {
        return eventRegistrationService.queryMyRegistrations();
    }
}
