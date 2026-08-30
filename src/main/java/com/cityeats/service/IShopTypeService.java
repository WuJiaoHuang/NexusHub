package com.cityeats.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cityeats.dto.Result;
import com.cityeats.entity.ShopType;

public interface IShopTypeService extends IService<ShopType> {
    Result querySort();
}
