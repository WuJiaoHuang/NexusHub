package com.campuslink.controller;

import com.campuslink.dto.Result;
import com.campuslink.entity.CampusEvent;
import com.campuslink.service.EventRecommendationService;
import com.campuslink.service.ICampusEventService;
import com.campuslink.service.IEventRegistrationService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/events")
public class CampusEventController {

    @Resource
    private ICampusEventService campusEventService;
    @Resource
    private IEventRegistrationService eventRegistrationService;
    @Resource
    private EventRecommendationService eventRecommendationService;

    @GetMapping
    public Result queryEvents(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return campusEventService.queryEvents(current);
    }

    @GetMapping("/{id}")
    public Result queryEventById(@PathVariable("id") Long id) {
        CampusEvent event = campusEventService.getById(id);
        return event == null ? Result.fail("校园活动不存在") : Result.ok(event);
    }

    @PostMapping
    public Result addCampusEvent(@RequestBody CampusEvent event) {
        campusEventService.addCampusEvent(event);
        return Result.ok(event.getId());
    }

    @GetMapping("/hot")
    public Result queryHotEvents(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return campusEventService.queryHotEvents(current);
    }

    @GetMapping("/by-place/{placeId}")
    public Result queryCampusEventOfCampusPlace(@PathVariable("placeId") Long placeId) {
       return campusEventService.queryCampusEventOfCampusPlace(placeId);
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
    public Result recommendations(@RequestParam(value = "limit", required = false) Integer limit) {
        return eventRecommendationService.recommend(limit);
    }
}
