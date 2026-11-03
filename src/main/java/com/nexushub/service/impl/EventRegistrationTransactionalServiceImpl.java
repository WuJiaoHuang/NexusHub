package com.nexushub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.nexushub.common.BusinessException;
import com.nexushub.entity.EventRegistration;
import com.nexushub.mapper.EventCapacityMapper;
import com.nexushub.mapper.EventRegistrationMapper;
import com.nexushub.service.IEventRegistrationTransactionalService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class EventRegistrationTransactionalServiceImpl implements IEventRegistrationTransactionalService {

    @Resource
    private EventRegistrationMapper eventRegistrationMapper;
    @Resource
    private EventCapacityMapper eventCapacityMapper;
    @Autowired(required = false)
    private RedissonClient redissonClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createEventRegistration(EventRegistration eventRegistration) {
        if (redissonClient != null) {
            RLock lock = redissonClient.getLock("lock:event:registration:" + eventRegistration.getEventId() + ":" + eventRegistration.getUserId());
            boolean locked = false;
            try {
                locked = lock.tryLock(1, 5, TimeUnit.SECONDS);
                if (!locked) {
                    log.warn("重复报名正在处理中，userId={}, eventId={}", eventRegistration.getUserId(), eventRegistration.getEventId());
                    return;
                }
                doCreateEventRegistration(eventRegistration);
                return;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("获取报名分布式锁被中断，userId={}, eventId={}", eventRegistration.getUserId(), eventRegistration.getEventId());
                return;
            } finally {
                if (locked && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
        doCreateEventRegistration(eventRegistration);
    }

    private void doCreateEventRegistration(EventRegistration eventRegistration) {
        Integer count = eventRegistrationMapper.selectCount(new QueryWrapper<EventRegistration>()
                .eq("user_id", eventRegistration.getUserId())
                .eq("event_id", eventRegistration.getEventId()));
        if (count != null && count > 0) {
            log.warn("重复报名被数据库事务拦截，userId={}, eventId={}", eventRegistration.getUserId(), eventRegistration.getEventId());
            return;
        }
        eventRegistration.setStatus(1);
        eventRegistration.setCreateTime(LocalDateTime.now());
        eventRegistration.setUpdateTime(LocalDateTime.now());
        try {
            eventRegistrationMapper.insert(eventRegistration);
        } catch (DuplicateKeyException e) {
            log.warn("唯一索引拦截重复报名，eventId={}, userId={}", eventRegistration.getEventId(), eventRegistration.getUserId());
            throw e;
        }
        int updated = eventCapacityMapper.update(null,
                new UpdateWrapper<com.nexushub.entity.EventCapacity>()
                        .setSql("remaining_slots = remaining_slots - 1")
                        .eq("event_id", eventRegistration.getEventId())
                        .gt("remaining_slots", 0));
        if (updated == 0) {
            log.warn("剩余名额不足，报名落库回滚，eventId={}", eventRegistration.getEventId());
            throw new BusinessException("活动剩余名额不足");
        }
        log.info("活动报名记录消费成功，registrationId={}, eventId={}, userId={}", eventRegistration.getId(), eventRegistration.getEventId(), eventRegistration.getUserId());
    }
}

