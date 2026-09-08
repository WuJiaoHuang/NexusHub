package com.campuslink.listener;

import com.campuslink.common.MqConstants;
import com.campuslink.dto.EventRegistrationMessage;
import com.campuslink.entity.EventRegistration;
import com.campuslink.service.IEventRegistrationTransactionalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class EventRegistrationListener {

    @Resource
    private IEventRegistrationTransactionalService transactionalService;

    @RabbitListener(queues = MqConstants.EVENT_REGISTRATION_QUEUE)
    public void handleEventRegistration(EventRegistrationMessage message) {
        EventRegistration eventRegistration = new EventRegistration();
        eventRegistration.setId(message.getRegistrationId());
        eventRegistration.setUserId(message.getUserId());
        eventRegistration.setEventId(message.getEventId());
        eventRegistration.setCreateTime(message.getCreateTime());
        try {
            transactionalService.createEventRegistration(eventRegistration);
        } catch (Exception e) {
            log.error("活动报名记录消费失败，order={}", eventRegistration, e);
            throw e;
        }
    }
}

