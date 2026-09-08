package com.campuslink.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.campuslink.dto.Result;
import com.campuslink.dto.UserDTO;
import com.campuslink.entity.CampusEvent;
import com.campuslink.service.EventRecommendationService;
import com.campuslink.service.ICampusEventService;
import com.campuslink.service.IUserInterestService;
import com.campuslink.utils.UserHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EventRecommendationServiceImpl implements EventRecommendationService {

    @Resource
    private ICampusEventService campusEventService;
    @Resource
    private IUserInterestService userInterestService;

    @Override
    public Result recommend(Integer limit) {
        int size = limit == null || limit <= 0 ? 10 : Math.min(limit, 50);
        UserDTO user = UserHolder.getUser();
        List<String> interests = user == null ? Collections.emptyList() : userInterestService.listTagsByUserId(user.getId());
        List<CampusEvent> events = campusEventService.list();
        if (CollUtil.isEmpty(events)) {
            return Result.ok(Collections.emptyList());
        }
        Set<String> interestSet = interests.stream().map(String::toLowerCase).collect(Collectors.toSet());
        List<CampusEvent> recommended = events.stream()
                .sorted(Comparator.comparingDouble((CampusEvent event) -> score(event, interestSet)).reversed())
                .limit(size)
                .collect(Collectors.toList());
        return Result.ok(recommended);
    }

    private double score(CampusEvent event, Set<String> interestSet) {
        double tagMatchScore = tagMatchScore(event.getTags(), interestSet);
        double popularityScore = popularityScore(event);
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

    private double popularityScore(CampusEvent event) {
        Integer capacity = event.getCapacity();
        Integer remainingSlots = event.getRemainingSlots();
        if (capacity == null || capacity <= 0 || remainingSlots == null) {
            return 0;
        }
        int registeredCount = Math.max(0, capacity - remainingSlots);
        return Math.min(1.0, registeredCount * 1.0 / capacity);
    }

    private double freshnessScore(CampusEvent event) {
        LocalDateTime anchor = event.getRegistrationStartTime() == null ? event.getCreateTime() : event.getRegistrationStartTime();
        if (anchor == null) {
            return 0;
        }
        long hours = Math.abs(Duration.between(LocalDateTime.now(), anchor).toHours());
        return Math.max(0, 1.0 - Math.min(hours, 168) / 168.0);
    }
}
