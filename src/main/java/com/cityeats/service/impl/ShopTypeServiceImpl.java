package com.cityeats.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cityeats.dto.Result;
import com.cityeats.entity.ShopType;
import com.cityeats.mapper.ShopTypeMapper;
import com.cityeats.service.IShopTypeService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    private static final String CACHE_SHOP_TYPE_KEY = "cache:shop:type";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result querySort() {
        String cached = stringRedisTemplate.opsForValue().get(CACHE_SHOP_TYPE_KEY);
        if (cached != null && cached.length() > 0) {
            return Result.ok(JSONUtil.toList(cached, ShopType.class));
        }
        List<ShopType> list = query().orderByAsc("sort").list();
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_TYPE_KEY, JSONUtil.toJsonStr(list), 30, TimeUnit.MINUTES);
        return Result.ok(list);
    }
}
