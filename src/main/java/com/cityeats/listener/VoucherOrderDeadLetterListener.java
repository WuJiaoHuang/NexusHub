package com.cityeats.listener;

import com.cityeats.common.MqConstants;
import com.cityeats.dto.VoucherOrderMessage;
import com.cityeats.entity.VoucherOrder;
import com.cityeats.mapper.VoucherOrderMapper;
import com.cityeats.service.VoucherOrderCompensationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class VoucherOrderDeadLetterListener {

    @Resource
    private VoucherOrderMapper voucherOrderMapper;
    @Resource
    private VoucherOrderCompensationService compensationService;

    @RabbitListener(queues = MqConstants.VOUCHER_ORDER_DLQ)
    public void handleDeadLetter(VoucherOrderMessage message) {
        VoucherOrder existing = voucherOrderMapper.selectById(message.getOrderId());
        if (existing != null) {
            log.warn("死信订单已在数据库落库，不执行 Redis 补偿，orderId={}", message.getOrderId());
            return;
        }
        compensationService.compensate(message);
    }
}
