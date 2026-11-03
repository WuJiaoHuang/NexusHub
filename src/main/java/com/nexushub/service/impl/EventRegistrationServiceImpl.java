package com.nexushub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nexushub.common.BusinessException;
import com.nexushub.common.MqConstants;
import com.nexushub.dto.EventRegistrationMessage;
import com.nexushub.dto.Result;
import com.nexushub.dto.UserDTO;
import com.nexushub.entity.EventCapacity;
import com.nexushub.entity.EventRegistration;
import com.nexushub.mapper.EventRegistrationMapper;
import com.nexushub.registration.RegistrationContext;
import com.nexushub.registration.RegistrationValidationChain;
import com.nexushub.service.IEventCapacityService;
import com.nexushub.service.IEventRegistrationService;
import com.nexushub.utils.RedisConstants;
import com.nexushub.utils.RedisIdWorker;
import com.nexushub.utils.UserHolder;
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

    @Value("${nexushub.registration.slots-warmup-enabled:true}")
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
