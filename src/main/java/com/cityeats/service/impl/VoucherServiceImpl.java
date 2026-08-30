package com.cityeats.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cityeats.dto.Result;
import com.cityeats.entity.SeckillVoucher;
import com.cityeats.entity.Voucher;
import com.cityeats.mapper.VoucherMapper;
import com.cityeats.service.ISeckillVoucherService;
import com.cityeats.service.IVoucherService;
import com.cityeats.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements IVoucherService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSeckillVoucher(Voucher voucher) {
        voucher.setCreateTime(LocalDateTime.now());
        voucher.setUpdateTime(LocalDateTime.now());
        save(voucher);
        if (voucher.getStock() != null && voucher.getBeginTime() != null && voucher.getEndTime() != null) {
            SeckillVoucher seckillVoucher = new SeckillVoucher();
            seckillVoucher.setVoucherId(voucher.getId());
            seckillVoucher.setStock(voucher.getStock());
            seckillVoucher.setBeginTime(voucher.getBeginTime());
            seckillVoucher.setEndTime(voucher.getEndTime());
            seckillVoucher.setCreateTime(LocalDateTime.now());
            seckillVoucher.setUpdateTime(LocalDateTime.now());
            seckillVoucherService.save(seckillVoucher);
            stringRedisTemplate.opsForValue().set(RedisConstants.SECKILL_STOCK_KEY + voucher.getId(), String.valueOf(voucher.getStock()));
        }
    }

    @Override
    public Result queryVoucherOfShop(Long shopId) {
        List<Voucher> vouchers = query().eq("shop_id", shopId).list();
        return Result.ok(vouchers);
    }
}
