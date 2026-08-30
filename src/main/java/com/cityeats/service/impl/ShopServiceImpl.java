package com.cityeats.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cityeats.cache.CacheStrategyFactory;
import com.cityeats.cache.CacheStrategyType;
import com.cityeats.dto.Result;
import com.cityeats.dto.ShopSearchRequest;
import com.cityeats.entity.Shop;
import com.cityeats.mapper.ShopMapper;
import com.cityeats.search.ShopChangedEvent;
import com.cityeats.search.ShopChangeType;
import com.cityeats.search.ShopSearchService;
import com.cityeats.service.IShopService;
import com.cityeats.utils.RedisConstants;
import com.cityeats.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private CacheStrategyFactory cacheStrategyFactory;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ShopSearchService shopSearchService;
    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Value("${cityeats.cache.shop-strategy:PASS_THROUGH}")
    private CacheStrategyType shopCacheStrategy;

    @Override
    public Result queryById(Long id) {
        Shop shop = cacheStrategyFactory.get(shopCacheStrategy).query(RedisConstants.CACHE_SHOP_KEY, id, Shop.class,
                this::getById, RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
        if (shop == null) {
            return Result.fail("商户不存在");
        }
        return Result.ok(shop);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result update(Shop shop) {
        if (shop.getId() == null) {
            return Result.fail("商户 id 不能为空");
        }
        boolean updated = updateById(shop);
        stringRedisTemplate.delete(RedisConstants.CACHE_SHOP_KEY + shop.getId());
        if (updated) {
            eventPublisher.publishEvent(new ShopChangedEvent(shop.getId(), ShopChangeType.UPDATED));
        }
        return Result.ok();
    }

    @Override
    public Result search(ShopSearchRequest request) {
        try {
            return Result.ok(shopSearchService.search(request));
        } catch (Exception e) {
            log.error("商户搜索失败，request={}", request, e);
            return Result.fail("商户搜索失败");
        }
    }

    @Override
    public Result queryShopByType(Integer typeId, Integer current, Double x, Double y) {
        if (x == null || y == null) {
            Page<Shop> page = query()
                    .eq("type_id", typeId)
                    .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
            return Result.ok(page.getRecords());
        }
        int from = (current - 1) * SystemConstants.DEFAULT_PAGE_SIZE;
        int end = current * SystemConstants.DEFAULT_PAGE_SIZE;
        String key = RedisConstants.SHOP_GEO_KEY + typeId;
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo()
                .radius(key, new Circle(new Point(x, y), new Distance(5, Metrics.KILOMETERS)),
                        RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeDistance().limit(end).sortAscending());
        if (results == null || CollUtil.isEmpty(results.getContent())) {
            return Result.ok(Collections.emptyList());
        }
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> list = results.getContent();
        if (list.size() <= from) {
            return Result.ok(Collections.emptyList());
        }
        List<Long> ids = new ArrayList<>(list.size());
        Map<String, Distance> distanceMap = list.stream().skip(from).map(result -> {
            String shopId = result.getContent().getName();
            ids.add(Long.valueOf(shopId));
            return result;
        }).collect(Collectors.toMap(result -> result.getContent().getName(), GeoResult::getDistance));
        String idStr = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        List<Shop> shops = query().in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list();
        shops.forEach(shop -> {
            Distance distance = distanceMap.get(shop.getId().toString());
            if (distance != null) {
                shop.setDistance(distance.getValue());
            }
        });
        return Result.ok(shops);
    }
}
