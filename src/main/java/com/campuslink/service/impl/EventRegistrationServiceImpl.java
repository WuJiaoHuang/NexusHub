package com.campuslink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campuslink.common.BusinessException;
import com.campuslink.common.MqConstants;
import com.campuslink.dto.EventRegistrationMessage;
import com.campuslink.dto.Result;
import com.campuslink.dto.UserDTO;
import com.campuslink.entity.EventCapacity;
import com.campuslink.entity.EventRegistration;
import com.campuslink.mapper.EventRegistrationMapper;
import com.campuslink.registration.RegistrationContext;
import com.campuslink.registration.RegistrationValidationChain;
import com.campuslink.service.IEventCapacityService;
import com.campuslink.service.IEventRegistrationService;
import com.campuslink.utils.RedisConstants;
import com.campuslink.utils.RedisIdWorker;
import com.campuslink.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;

@Slf4j
@Service
public class EventRegistrationServiceImpl extends ServiceImpl<EventRegistrationMapper, EventRegistration> implements IEventRegistrationService {

    private static final DefaultRedisScript<Long> REGISTRATION_SCRIPT = new DefaultRedisScript<>();

    @Resource
    private IEventCapacityService eventCapacityService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedisIdWorker redisIdWorker;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private RegistrationValidationChain registrationValidationChain;

    @Value("${campuslink.registration.slots-warmup-enabled:true}")
    private boolean slotsWarmupEnabled;

    static {
        REGISTRATION_SCRIPT.setLocation(new ClassPathResource("lua/event_registration.lua"));
        REGISTRATION_SCRIPT.setResultType(Long.class);
    }

    @PostConstruct
    public void warmUpEventSlots() {
        if (!slotsWarmupEnabled) {
            return;
        }
        eventCapacityService.list().forEach(capacity ->
                stringRedisTemplate.opsForValue().set(RedisConstants.EVENT_REMAINING_SLOTS_KEY + capacity.getEventId(), String.valueOf(capacity.getRemainingSlots())));
    }

    @Override
    public Result registerForEvent(Long eventId) {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("请先登录");
        }
        EventCapacity capacity = eventCapacityService.getById(eventId);
        RegistrationContext context = new RegistrationContext();
        context.setEventId(eventId);
        context.setUserId(user.getId());
        context.setNow(LocalDateTime.now());
        context.setEventCapacity(capacity);
        try {
            registrationValidationChain.validate(context);
        } catch (BusinessException e) {
            return Result.fail(e.getMessage());
        }
        long registrationId = redisIdWorker.nextId("event_registration");
        Long result = stringRedisTemplate.execute(REGISTRATION_SCRIPT, Collections.emptyList(), eventId.toString(), user.getId().toString());
        if (result == null || result != 0) {
            int r = result == null ? -1 : result.intValue();
            return Result.fail(r == 1 ? "活动剩余名额不足" : "不能重复报名");
        }
        EventRegistrationMessage message = new EventRegistrationMessage(registrationId, user.getId(), eventId, LocalDateTime.now());
        rabbitTemplate.convertAndSend(MqConstants.EVENT_REGISTRATION_EXCHANGE, MqConstants.EVENT_REGISTRATION_ROUTING_KEY, message);
        log.info("热门活动报名资格校验通过，报名消息已发送，registrationId={}, eventId={}, userId={}", registrationId, eventId, user.getId());
        return Result.ok(registrationId);
    }

    @Override
    public Result queryMyRegistrationStatus(Long eventId) {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("请先登录");
        }
        Integer count = baseMapper.selectCount(new QueryWrapper<EventRegistration>()
                .eq("user_id", user.getId())
                .eq("event_id", eventId));
        return Result.ok(count != null && count > 0);
    }

    @Override
    public Result queryMyRegistrations() {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("请先登录");
        }
        return Result.ok(query().eq("user_id", user.getId()).orderByDesc("create_time").list());
    }
}
