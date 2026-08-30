package com.cityeats.seckill;

import com.cityeats.common.BusinessException;
import com.cityeats.entity.SeckillVoucher;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeckillValidationChainTest {

    @Test
    void chainShouldStopWhenVoucherMissing() {
        SeckillContext context = new SeckillContext();
        context.setVoucherId(1L);
        context.setNow(LocalDateTime.now());
        SeckillValidationChain chain = new SeckillValidationChain(Arrays.asList(
                new VoucherExistValidator(),
                new SeckillTimeValidator()));

        assertThatThrownBy(() -> chain.validate(context))
                .isInstanceOf(BusinessException.class)
                .hasMessage("优惠券不存在");
    }

    @Test
    void chainShouldRejectNotStartedVoucher() {
        SeckillVoucher voucher = new SeckillVoucher();
        voucher.setBeginTime(LocalDateTime.now().plusMinutes(10));
        voucher.setEndTime(LocalDateTime.now().plusHours(1));
        SeckillContext context = new SeckillContext();
        context.setVoucherId(1L);
        context.setNow(LocalDateTime.now());
        context.setSeckillVoucher(voucher);
        SeckillValidationChain chain = new SeckillValidationChain(Arrays.asList(
                new VoucherExistValidator(),
                new SeckillTimeValidator()));

        assertThatThrownBy(() -> chain.validate(context))
                .isInstanceOf(BusinessException.class)
                .hasMessage("秒杀尚未开始");
    }
}
