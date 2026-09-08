package com.campuslink.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campuslink.dto.Result;
import com.campuslink.entity.PlaceCategory;
import com.campuslink.mapper.PlaceCategoryMapper;
import com.campuslink.service.IPlaceCategoryService;
import com.campuslink.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class PlaceCategoryServiceImpl extends ServiceImpl<PlaceCategoryMapper, PlaceCategory> implements IPlaceCategoryService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result querySort() {
        String cached = stringRedisTemplate.opsForValue().get(RedisConstants.CACHE_PLACE_CATEGORY_KEY);
        if (cached != null && cached.length() > 0) {
            return Result.ok(JSONUtil.toList(cached, PlaceCategory.class));
        }
        List<PlaceCategory> list = query().orderByAsc("sort").list();
        stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_PLACE_CATEGORY_KEY, JSONUtil.toJsonStr(list), 30, TimeUnit.MINUTES);
        return Result.ok(list);
    }
}
