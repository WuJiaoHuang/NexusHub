package com.nexushub.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nexushub.dto.Result;
import com.nexushub.entity.Event;
import com.nexushub.entity.EventCapacity;
import com.nexushub.mapper.EventMapper;
import com.nexushub.service.IEventService;
import com.nexushub.service.IEventCapacityService;
import com.nexushub.utils.RedisConstants;
import com.nexushub.utils.SystemConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl extends ServiceImpl<EventMapper, Event> implements IEventService {

    @Resource
    private IEventCapacityService eventCapacityService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addEvent(Event event) {
        LocalDateTime now = LocalDateTime.now();
        event.setCreateTime(now);
        event.setUpdateTime(now);
        save(event);
        if (event.getCapacity() != null && event.getRegistrationStartTime() != null && event.getRegistrationEndTime() != null) {
            EventCapacity capacity = new EventCapacity()
                    .setEventId(event.getId())
                    .setRemainingSlots(event.getCapacity())
                    .setRegistrationStartTime(event.getRegistrationStartTime())
                    .setRegistrationEndTime(event.getRegistrationEndTime())
                    .setCreateTime(now)
                    .setUpdateTime(now);
            eventCapacityService.save(capacity);
            stringRedisTemplate.opsForValue().set(RedisConstants.EVENT_REMAINING_SLOTS_KEY + event.getId(), String.valueOf(event.getCapacity()));
        }
    }

    @Override
    public Result queryEvents(Integer current) {
        Page<Event> page = query()
                .orderByDesc("create_time")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        return Result.ok(page.getRecords());
    }

    @Override
    public Result queryHotEvents(Integer current) {
        List<Event> events = list();
        Map<Long, Integer> remainingSlots = eventCapacityService.list().stream()
                .collect(Collectors.toMap(EventCapacity::getEventId, EventCapacity::getRemainingSlots, (left, right) -> left));
        List<Event> sorted = events.stream()
                .sorted(Comparator
                        .comparingInt((Event event) -> remainingSlots.getOrDefault(event.getId(), Integer.MAX_VALUE))
                        .thenComparing(Event::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .skip((long) (current - 1) * SystemConstants.MAX_PAGE_SIZE)
                .limit(SystemConstants.MAX_PAGE_SIZE)
                .collect(Collectors.toList());
        return Result.ok(sorted);
    }

    @Override
    public Result queryEventOfPlace(Long placeId) {
        List<Event> events = query().eq("place_id", placeId).list();
        return Result.ok(events);
    }
}
