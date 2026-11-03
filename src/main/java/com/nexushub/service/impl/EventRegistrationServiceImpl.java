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
import com.nexushub.service.EventRegistrationCompensationService;
import com.nexushub.utils.RedisConstants;
import com.nexushub.utils.RedisIdWorker;
import com.nexushub.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
    @Resource
    private EventRegistrationCompensationService compensationService;

    @Value("${nexushub.registration.publish-confirm-timeout-ms:5000}")
    private long publishConfirmTimeoutMs;

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
        CorrelationData correlation = new CorrelationData(String.valueOf(registrationId));
        try {
            rabbitTemplate.convertAndSend(MqConstants.EVENT_REGISTRATION_EXCHANGE,
                    MqConstants.EVENT_REGISTRATION_ROUTING_KEY, message, correlation);
            CorrelationData.Confirm confirm = correlation.getFuture()
                    .get(Math.max(1, publishConfirmTimeoutMs), TimeUnit.MILLISECONDS);
            if (correlation.getReturnedMessage() != null
                    || (!confirm.isAck() && confirm.getReason() == null)) {
                return compensateRejectedPublish(message);
            }
            if (!confirm.isAck()) {
                // A framework-generated nack (e.g. connection close) does not prove non-delivery.
                return uncertainPublish(message, new IllegalStateException(confirm.getReason()));
            }
            log.info("报名消息已获 broker 确认，registrationId={}, eventId={}, userId={}", registrationId, eventId, user.getId());
            return Result.ok(registrationId);
        } catch (AmqpConnectException | MessageConversionException notSent) {
            log.warn("报名消息在发布前失败，registrationId={}", registrationId, notSent);
            return compensateRejectedPublish(message);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            return uncertainPublish(message, interrupted);
        } catch (TimeoutException | ExecutionException uncertain) {
            return uncertainPublish(message, uncertain);
        } catch (RuntimeException uncertain) {
            return uncertainPublish(message, uncertain);
        }
    }

    private Result compensateRejectedPublish(EventRegistrationMessage message) {
        try {
            Long result = compensationService.compensate(message);
            if (result == null || (result != 0L && result != 1L)) {
                throw new IllegalStateException("未确认 Redis 名额补偿结果");
            }
            return Result.fail("报名消息发送失败，名额已恢复，请重试");
        } catch (RuntimeException compensationFailure) {
            log.error("报名消息未投递且补偿失败，需要人工核对，registrationId={}, eventId={}, userId={}",
                    message.getRegistrationId(), message.getEventId(), message.getUserId(), compensationFailure);
            return Result.fail("报名提交失败，名额恢复待处理，请稍后查询报名状态");
        }
    }

    private Result uncertainPublish(EventRegistrationMessage message, Exception cause) {
        log.error("报名投递结果不确定，保留预占名额，需要核对，registrationId={}, eventId={}, userId={}",
                message.getRegistrationId(), message.getEventId(), message.getUserId(), cause);
        return Result.fail("报名发送结果待确认，请稍后查询报名状态");
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
