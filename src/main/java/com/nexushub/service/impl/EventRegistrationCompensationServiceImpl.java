package com.nexushub.service.impl;

import com.nexushub.dto.EventRegistrationMessage;
import com.nexushub.service.EventRegistrationCompensationService;
import com.nexushub.utils.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;

@Slf4j
@Service
public class EventRegistrationCompensationServiceImpl implements EventRegistrationCompensationService {

    private static final DefaultRedisScript<Long> COMPENSATE_SCRIPT = new DefaultRedisScript<>();

    static {
        COMPENSATE_SCRIPT.setLocation(new ClassPathResource("lua/compensate_event_registration.lua"));
        COMPENSATE_SCRIPT.setResultType(Long.class);
    }

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Long compensate(EventRegistrationMessage message) {
        Long result = stringRedisTemplate.execute(COMPENSATE_SCRIPT, Collections.emptyList(),
                RedisConstants.EVENT_REMAINING_SLOTS_KEY + message.getEventId(),
                RedisConstants.EVENT_REGISTRATION_KEY + message.getEventId(),
                RedisConstants.EVENT_REGISTRATION_COMPENSATED_KEY + message.getRegistrationId(),
                String.valueOf(message.getUserId()),
                "86400");
        log.warn("热门报名失败报名记录 Redis 补偿完成，registrationId={}, eventId={}, userId={}, result={}",
                message.getRegistrationId(), message.getEventId(), message.getUserId(), result);
        return result;
    }
}

