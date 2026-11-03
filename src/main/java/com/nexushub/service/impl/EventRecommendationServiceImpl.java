package com.nexushub.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.nexushub.dto.Result;
import com.nexushub.dto.UserDTO;
import com.nexushub.entity.Event;
import com.nexushub.entity.EventCapacity;
import com.nexushub.service.EventRecommendationService;
import com.nexushub.service.IEventService;
import com.nexushub.service.IEventCapacityService;
import com.nexushub.service.IUserInterestService;
import com.nexushub.utils.UserHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EventRecommendationServiceImpl implements EventRecommendationService {

    @Resource
    private IEventService eventService;
    @Resource
    private IEventCapacityService eventCapacityService;
    @Resource
    private IUserInterestService userInterestService;

    @Override
    public Result recommend(Integer limit) {
        int size = limit == null || limit <= 0 ? 10 : Math.min(limit, 50);
        UserDTO user = UserHolder.getUser();
        List<String> interests = user == null ? Collections.emptyList() : userInterestService.listTagsByUserId(user.getId());
        List<Event> events = eventService.list();
        if (CollUtil.isEmpty(events)) {
            return Result.ok(Collections.emptyList());
        }
        Set<String> interestSet = interests.stream().map(String::toLowerCase).collect(Collectors.toSet());
        Map<Long, EventCapacity> capacities = eventCapacityService.list().stream()
                .collect(Collectors.toMap(EventCapacity::getEventId, capacity -> capacity, (left, right) -> left));
        List<Event> recommended = events.stream()
                .sorted(Comparator.comparingDouble((Event event) -> score(event, capacities.get(event.getId()), interestSet)).reversed())
                .limit(size)
                .collect(Collectors.toList());
        return Result.ok(recommended);
    }

    private double score(Event event, EventCapacity capacity, Set<String> interestSet) {
        double tagMatchScore = tagMatchScore(event.getTags(), interestSet);
        double popularityScore = popularityScore(event, capacity);
        double freshnessScore = freshnessScore(event);
        return tagMatchScore * 0.5 + popularityScore * 0.3 + freshnessScore * 0.2;
    }

    private double tagMatchScore(String tags, Set<String> interestSet) {
        if (StrUtil.isBlank(tags) || CollUtil.isEmpty(interestSet)) {
            return 0;
        }
        List<String> eventTags = Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        if (eventTags.isEmpty()) {
            return 0;
        }
        long matches = eventTags.stream().filter(interestSet::contains).count();
        return Math.min(1.0, matches * 1.0 / eventTags.size());
    }

    private double popularityScore(Event event, EventCapacity capacity) {
        Integer totalCapacity = event.getCapacity();
        Integer remainingSlots = capacity == null ? null : capacity.getRemainingSlots();
        if (totalCapacity == null || totalCapacity <= 0 || remainingSlots == null) {
            return 0;
        }
        int registeredCount = Math.max(0, totalCapacity - remainingSlots);
        return Math.min(1.0, registeredCount * 1.0 / totalCapacity);
    }

    private double freshnessScore(Event event) {
        LocalDateTime anchor = event.getRegistrationStartTime() == null ? event.getCreateTime() : event.getRegistrationStartTime();
        if (anchor == null) {
            return 0;
        }
        long hours = Math.abs(Duration.between(LocalDateTime.now(), anchor).toHours());
        return Math.max(0, 1.0 - Math.min(hours, 168) / 168.0);
    }
}
