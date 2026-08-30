package com.cityeats.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cityeats.entity.VoucherOrder;
import com.cityeats.mapper.SeckillVoucherMapper;
import com.cityeats.mapper.VoucherOrderMapper;
import com.cityeats.service.IVoucherOrderTransactionalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Slf4j
@Service
public class VoucherOrderTransactionalServiceImpl implements IVoucherOrderTransactionalService {

    @Resource
    private VoucherOrderMapper voucherOrderMapper;
    @Resource
    private SeckillVoucherMapper seckillVoucherMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createVoucherOrder(VoucherOrder voucherOrder) {
        Integer count = voucherOrderMapper.selectCount(new LambdaQueryWrapper<VoucherOrder>()
                .eq(VoucherOrder::getUserId, voucherOrder.getUserId())
                .eq(VoucherOrder::getVoucherId, voucherOrder.getVoucherId()));
        if (count != null && count > 0) {
            log.warn("重复下单被数据库事务拦截，userId={}, voucherId={}", voucherOrder.getUserId(), voucherOrder.getVoucherId());
            return;
        }
        int updated = seckillVoucherMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<com.cityeats.entity.SeckillVoucher>()
                        .setSql("stock = stock - 1")
                        .eq(com.cityeats.entity.SeckillVoucher::getVoucherId, voucherOrder.getVoucherId())
                        .gt(com.cityeats.entity.SeckillVoucher::getStock, 0));
        if (updated == 0) {
            log.warn("库存不足，订单落库终止，voucherId={}", voucherOrder.getVoucherId());
            return;
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
