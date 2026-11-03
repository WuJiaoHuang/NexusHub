package com.nexushub.controller;

import com.nexushub.security.PublicEndpoint;
import com.nexushub.security.AdminOnly;

import com.nexushub.dto.Result;
import com.nexushub.entity.Event;
import com.nexushub.service.EventRecommendationService;
import com.nexushub.service.IEventService;
import com.nexushub.service.IEventRegistrationService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/events")
public class EventController {

    @Resource
    private IEventService eventService;
    @Resource
    private IEventRegistrationService eventRegistrationService;
    @Resource
    private EventRecommendationService eventRecommendationService;

    @GetMapping
    @PublicEndpoint
    public Result queryEvents(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return eventService.queryEvents(current);
    }

    @GetMapping("/{id}")
    @PublicEndpoint
    public Result queryEventById(@PathVariable("id") Long id) {
        Event event = eventService.getById(id);
        return event == null ? Result.fail("活动不存在") : Result.ok(event);
    }

    @PostMapping
    @AdminOnly
    public Result addEvent(@RequestBody Event event) {
        eventService.addEvent(event);
        return Result.ok(event.getId());
    }

    @GetMapping("/hot")
    @PublicEndpoint
    public Result queryHotEvents(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return eventService.queryHotEvents(current);
    }

    @GetMapping("/by-place/{placeId}")
    @PublicEndpoint
    public Result queryEventOfPlace(@PathVariable("placeId") Long placeId) {
       return eventService.queryEventOfPlace(placeId);
    }

    @PostMapping("/{eventId}/registrations")
    public Result register(@PathVariable("eventId") Long eventId) {
        return eventRegistrationService.registerForEvent(eventId);
    }

    @GetMapping("/{eventId}/registrations/status")
    public Result registrationStatus(@PathVariable("eventId") Long eventId) {
        return eventRegistrationService.queryMyRegistrationStatus(eventId);
    }

    @GetMapping("/recommendations")
    @PublicEndpoint
    public Result recommendations(@RequestParam(value = "limit", required = false) Integer limit) {
        return eventRecommendationService.recommend(limit);
    }
}
