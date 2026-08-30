package com.cityeats.seckill;

import com.cityeats.entity.SeckillVoucher;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SeckillContext {
    private Long voucherId;
    private Long userId;
    private LocalDateTime now;
    private SeckillVoucher seckillVoucher;
}
