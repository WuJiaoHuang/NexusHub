package com.cityeats.seckill;

import com.cityeats.common.BusinessException;
import com.cityeats.entity.SeckillVoucher;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(20)
@Component
public class SeckillTimeValidator implements SeckillValidator {
    @Override
    public void validate(SeckillContext context) {
        SeckillVoucher voucher = context.getSeckillVoucher();
        if (voucher.getBeginTime().isAfter(context.getNow())) {
            throw new BusinessException("秒杀尚未开始");
        }
        if (voucher.getEndTime().isBefore(context.getNow())) {
            throw new BusinessException("秒杀已经结束");
        }
    }
}
