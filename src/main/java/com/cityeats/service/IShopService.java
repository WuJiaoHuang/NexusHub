package com.cityeats.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cityeats.dto.Result;
import com.cityeats.dto.ShopSearchRequest;
import com.cityeats.entity.Shop;

public interface IShopService extends IService<Shop> {
    Result queryById(Long id);
    Result update(Shop shop);
    Result queryShopByType(Integer typeId, Integer current, Double x, Double y);
    Result search(ShopSearchRequest request);
}
