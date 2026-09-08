package com.campuslink.listener;

import com.campuslink.common.MqConstants;
import com.campuslink.dto.EventRegistrationMessage;
import com.campuslink.entity.EventRegistration;
import com.campuslink.mapper.EventRegistrationMapper;
import com.campuslink.service.EventRegistrationCompensationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class EventRegistrationDeadLetterListener {

    @Resource
    private EventRegistrationMapper eventRegistrationMapper;
    @Resource
    private EventRegistrationCompensationService compensationService;

    @RabbitListener(queues = MqConstants.EVENT_REGISTRATION_DLQ)
    public void handleDeadLetter(EventRegistrationMessage message) {
        EventRegistration existing = eventRegistrationMapper.selectById(message.getRegistrationId());
        if (existing != null) {
            log.warn("死信报名记录已在数据库落库，不执行 Redis 补偿，registrationId={}", message.getRegistrationId());
            return;
        }
        compensationService.compensate(message);
    }
}

