package com.campuslink.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campuslink.dto.Result;
import com.campuslink.entity.EventCapacity;
import com.campuslink.entity.CampusEvent;
import com.campuslink.mapper.CampusEventMapper;
import com.campuslink.service.IEventCapacityService;
import com.campuslink.service.ICampusEventService;
import com.campuslink.utils.RedisConstants;
import com.campuslink.utils.SystemConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

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
        if (event.getRemainingSlots() == null) {
            event.setRemainingSlots(event.getCapacity());
        }
        save(event);
        if (event.getRemainingSlots() != null && event.getRegistrationStartTime() != null && event.getRegistrationEndTime() != null) {
            EventCapacity capacity = new EventCapacity()
                    .setEventId(event.getId())
                    .setRemainingSlots(event.getRemainingSlots())
                    .setRegistrationStartTime(event.getRegistrationStartTime())
                    .setRegistrationEndTime(event.getRegistrationEndTime())
                    .setCreateTime(now)
                    .setUpdateTime(now);
            eventCapacityService.save(capacity);
            stringRedisTemplate.opsForValue().set(RedisConstants.EVENT_REMAINING_SLOTS_KEY + event.getId(), String.valueOf(event.getRemainingSlots()));
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
        Page<CampusEvent> page = query()
                .orderByAsc("remaining_slots")
                .orderByDesc("create_time")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        return Result.ok(page.getRecords());
    }

    @Override
    public Result queryCampusEventOfCampusPlace(Long placeId) {
        List<CampusEvent> events = query().eq("place_id", placeId).list();
        return Result.ok(events);
    }
}
