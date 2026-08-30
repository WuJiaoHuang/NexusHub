package com.cityeats.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cityeats.dto.Result;
import com.cityeats.entity.Voucher;

public interface IVoucherService extends IService<Voucher> {
    void addSeckillVoucher(Voucher voucher);
    Result queryVoucherOfShop(Long shopId);
}
