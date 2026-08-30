package com.cityeats.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cityeats.common.BusinessException;
import com.cityeats.common.MqConstants;
import com.cityeats.dto.VoucherOrderMessage;
import com.cityeats.dto.Result;
import com.cityeats.dto.UserDTO;
import com.cityeats.entity.SeckillVoucher;
import com.cityeats.entity.VoucherOrder;
import com.cityeats.mapper.VoucherOrderMapper;
import com.cityeats.seckill.SeckillContext;
import com.cityeats.seckill.SeckillValidationChain;
import com.cityeats.service.ISeckillVoucherService;
import com.cityeats.service.IVoucherOrderService;
import com.cityeats.utils.RedisIdWorker;
import com.cityeats.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;

@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT = new DefaultRedisScript<>();

    @Resource
    private ISeckillVoucherService seckillVoucherService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedisIdWorker redisIdWorker;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private SeckillValidationChain seckillValidationChain;

    @Value("${cityeats.seckill.stock-warmup-enabled:true}")
    private boolean stockWarmupEnabled;

    static {
        SECKILL_SCRIPT.setLocation(new ClassPathResource("lua/seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    @PostConstruct
    public void warmUpSeckillStock() {
        if (!stockWarmupEnabled) {
            return;
        }
        seckillVoucherService.list().forEach(voucher ->
                stringRedisTemplate.opsForValue().set("seckill:stock:" + voucher.getVoucherId(), String.valueOf(voucher.getStock())));
    }

    @Override
    public Result seckillVoucher(Long voucherId) {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("请先登录");
        }
        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);
        SeckillContext context = new SeckillContext();
        context.setVoucherId(voucherId);
        context.setUserId(user.getId());
        context.setNow(LocalDateTime.now());
        context.setSeckillVoucher(voucher);
        try {
            seckillValidationChain.validate(context);
        } catch (BusinessException e) {
            return Result.fail(e.getMessage());
        }
        long orderId = redisIdWorker.nextId("order");
        Long result = stringRedisTemplate.execute(SECKILL_SCRIPT, Collections.emptyList(),
                voucherId.toString(), user.getId().toString());
        if (result == null || result != 0) {
            int r = result == null ? -1 : result.intValue();
            return Result.fail(r == 1 ? "库存不足" : "不能重复下单");
        }
        VoucherOrderMessage order = new VoucherOrderMessage(orderId, user.getId(), voucherId, LocalDateTime.now());
        rabbitTemplate.convertAndSend(MqConstants.VOUCHER_ORDER_EXCHANGE, MqConstants.VOUCHER_ORDER_ROUTING_KEY, order);
        log.info("秒杀资格校验通过，订单消息已发送，orderId={}, voucherId={}, userId={}", orderId, voucherId, user.getId());
        return Result.ok(orderId);
    }
}
