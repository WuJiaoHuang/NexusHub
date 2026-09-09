package com.campuslink.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campuslink.dto.Result;
import com.campuslink.entity.CampusEvent;
import com.campuslink.entity.EventCapacity;
import com.campuslink.mapper.CampusEventMapper;
import com.campuslink.service.ICampusEventService;
import com.campuslink.service.IEventCapacityService;
import com.campuslink.utils.RedisConstants;
import com.campuslink.utils.SystemConstants;
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
public class CampusEventServiceImpl extends ServiceImpl<CampusEventMapper, CampusEvent> implements ICampusEventService {

    @Resource
    private IEventCapacityService eventCapacityService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCampusEvent(CampusEvent event) {
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
        Page<CampusEvent> page = query()
                .orderByDesc("create_time")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        return Result.ok(page.getRecords());
    }

    @Override
    public Result queryHotEvents(Integer current) {
        List<CampusEvent> events = list();
        Map<Long, Integer> remainingSlots = eventCapacityService.list().stream()
                .collect(Collectors.toMap(EventCapacity::getEventId, EventCapacity::getRemainingSlots, (left, right) -> left));
        List<CampusEvent> sorted = events.stream()
                .sorted(Comparator
                        .comparingInt((CampusEvent event) -> remainingSlots.getOrDefault(event.getId(), Integer.MAX_VALUE))
                        .thenComparing(CampusEvent::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .skip((long) (current - 1) * SystemConstants.MAX_PAGE_SIZE)
                .limit(SystemConstants.MAX_PAGE_SIZE)
                .collect(Collectors.toList());
        return Result.ok(sorted);
    }

    @Override
    public Result queryCampusEventOfCampusPlace(Long placeId) {
        List<CampusEvent> events = query().eq("place_id", placeId).list();
        return Result.ok(events);
    }
}
