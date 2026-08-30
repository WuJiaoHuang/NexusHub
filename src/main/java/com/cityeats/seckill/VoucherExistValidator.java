package com.cityeats.seckill;

import com.cityeats.common.BusinessException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(10)
@Component
public class VoucherExistValidator implements SeckillValidator {
    @Override
    public void validate(SeckillContext context) {
        if (context.getVoucherId() == null) {
            throw new BusinessException("优惠券 id 不能为空");
        }
        if (context.getSeckillVoucher() == null) {
            throw new BusinessException("优惠券不存在");
        }
    }
}
