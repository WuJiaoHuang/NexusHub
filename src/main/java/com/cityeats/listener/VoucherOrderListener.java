package com.cityeats.listener;

import com.cityeats.common.MqConstants;
import com.cityeats.dto.VoucherOrderMessage;
import com.cityeats.entity.VoucherOrder;
import com.cityeats.service.IVoucherOrderTransactionalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class VoucherOrderListener {

    @Resource
    private IVoucherOrderTransactionalService transactionalService;

    @RabbitListener(queues = MqConstants.VOUCHER_ORDER_QUEUE)
    public void handleVoucherOrder(VoucherOrderMessage message) {
        VoucherOrder voucherOrder = new VoucherOrder();
        voucherOrder.setId(message.getOrderId());
        voucherOrder.setUserId(message.getUserId());
        voucherOrder.setVoucherId(message.getVoucherId());
        voucherOrder.setCreateTime(message.getCreateTime());
        try {
            transactionalService.createVoucherOrder(voucherOrder);
        } catch (Exception e) {
            log.error("优惠券订单消费失败，order={}", voucherOrder, e);
            throw e;
        }
    }
}
