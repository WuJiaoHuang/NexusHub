package com.cityeats.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.cityeats.common.BusinessException;
import com.cityeats.entity.VoucherOrder;
import com.cityeats.mapper.SeckillVoucherMapper;
import com.cityeats.mapper.VoucherOrderMapper;
import com.cityeats.service.IVoucherOrderTransactionalService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class VoucherOrderTransactionalServiceImpl implements IVoucherOrderTransactionalService {

    @Resource
    private VoucherOrderMapper voucherOrderMapper;
    @Resource
    private SeckillVoucherMapper seckillVoucherMapper;
    @Autowired(required = false)
    private RedissonClient redissonClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createVoucherOrder(VoucherOrder voucherOrder) {
        if (redissonClient != null) {
            RLock lock = redissonClient.getLock("lock:order:" + voucherOrder.getVoucherId() + ":" + voucherOrder.getUserId());
            boolean locked = false;
            try {
                locked = lock.tryLock(1, 5, TimeUnit.SECONDS);
                if (!locked) {
                    log.warn("重复下单正在处理中，userId={}, voucherId={}", voucherOrder.getUserId(), voucherOrder.getVoucherId());
                    return;
                }
                doCreateVoucherOrder(voucherOrder);
                return;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("获取订单分布式锁被中断，userId={}, voucherId={}", voucherOrder.getUserId(), voucherOrder.getVoucherId());
                return;
            } finally {
                if (locked && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
        doCreateVoucherOrder(voucherOrder);
    }

    private void doCreateVoucherOrder(VoucherOrder voucherOrder) {
        Integer count = voucherOrderMapper.selectCount(new QueryWrapper<VoucherOrder>()
                .eq("user_id", voucherOrder.getUserId())
                .eq("voucher_id", voucherOrder.getVoucherId()));
        if (count != null && count > 0) {
            log.warn("重复下单被数据库事务拦截，userId={}, voucherId={}", voucherOrder.getUserId(), voucherOrder.getVoucherId());
            return;
        }
        int updated = seckillVoucherMapper.update(null,
                new UpdateWrapper<com.cityeats.entity.SeckillVoucher>()
                        .setSql("stock = stock - 1")
                        .eq("voucher_id", voucherOrder.getVoucherId())
                        .gt("stock", 0));
        if (updated == 0) {
            log.warn("库存不足，订单落库终止，voucherId={}", voucherOrder.getVoucherId());
            throw new BusinessException("优惠券库存不足");
        }
        voucherOrder.setStatus(1);
        voucherOrder.setCreateTime(LocalDateTime.now());
        voucherOrder.setUpdateTime(LocalDateTime.now());
        try {
            voucherOrderMapper.insert(voucherOrder);
            log.info("优惠券订单消费成功，orderId={}, voucherId={}, userId={}", voucherOrder.getId(), voucherOrder.getVoucherId(), voucherOrder.getUserId());
        } catch (DuplicateKeyException e) {
            log.warn("唯一索引拦截重复订单，voucherId={}, userId={}", voucherOrder.getVoucherId(), voucherOrder.getUserId());
        }
    }
}
