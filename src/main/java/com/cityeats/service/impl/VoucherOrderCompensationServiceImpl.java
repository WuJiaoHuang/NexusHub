package com.cityeats.service.impl;

import com.cityeats.dto.VoucherOrderMessage;
import com.cityeats.service.VoucherOrderCompensationService;
import com.cityeats.utils.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;

@Slf4j
@Service
public class VoucherOrderCompensationServiceImpl implements VoucherOrderCompensationService {

    private static final DefaultRedisScript<Long> COMPENSATE_SCRIPT = new DefaultRedisScript<>();

    static {
        COMPENSATE_SCRIPT.setLocation(new ClassPathResource("lua/compensate_seckill.lua"));
        COMPENSATE_SCRIPT.setResultType(Long.class);
    }

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Long compensate(VoucherOrderMessage message) {
        Long result = stringRedisTemplate.execute(COMPENSATE_SCRIPT, Collections.emptyList(),
                RedisConstants.SECKILL_STOCK_KEY + message.getVoucherId(),
                RedisConstants.SECKILL_ORDER_KEY + message.getVoucherId(),
                RedisConstants.SECKILL_COMPENSATED_KEY + message.getOrderId(),
                String.valueOf(message.getUserId()),
                "86400");
        log.warn("秒杀失败订单 Redis 补偿完成，orderId={}, voucherId={}, userId={}, result={}",
                message.getOrderId(), message.getVoucherId(), message.getUserId(), result);
        return result;
    }
}
