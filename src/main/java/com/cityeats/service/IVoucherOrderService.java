package com.cityeats.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cityeats.dto.Result;
import com.cityeats.entity.VoucherOrder;

public interface IVoucherOrderService extends IService<VoucherOrder> {
    Result seckillVoucher(Long voucherId);
}
