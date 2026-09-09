package com.campuslink.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campuslink.cache.CacheStrategyFactory;
import com.campuslink.cache.CacheStrategyType;
import com.campuslink.dto.Result;
import com.campuslink.dto.CampusPlaceSearchRequest;
import com.campuslink.entity.CampusPlace;
import com.campuslink.mapper.CampusPlaceMapper;
import com.campuslink.search.CampusPlaceChangedEvent;
import com.campuslink.search.CampusPlaceChangeType;
import com.campuslink.search.CampusPlaceSearchService;
import com.campuslink.service.ICampusPlaceService;
import com.campuslink.utils.RedisConstants;
import com.campuslink.utils.SystemConstants;
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
public class CampusPlaceServiceImpl extends ServiceImpl<CampusPlaceMapper, CampusPlace> implements ICampusPlaceService {

    @Resource
    private CacheStrategyFactory cacheStrategyFactory;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private CampusPlaceSearchService placeSearchService;
    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Value("${campuslink.cache.place-strategy:PASS_THROUGH}")
    private CacheStrategyType placeCacheStrategy;

    @Override
    public Result queryById(Long id) {
        CampusPlace place = cacheStrategyFactory.get(placeCacheStrategy).query(RedisConstants.CACHE_PLACE_KEY, id, CampusPlace.class,
                this::getById, RedisConstants.CACHE_PLACE_TTL, TimeUnit.MINUTES);
        if (place == null) {
            return Result.fail("校园地点不存在");
        }
        return Result.ok(place);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result update(CampusPlace place) {
        if (place.getId() == null) {
            return Result.fail("校园地点 id 不能为空");
        }
        boolean updated = updateById(place);
        stringRedisTemplate.delete(RedisConstants.CACHE_PLACE_KEY + place.getId());
        if (updated) {
            eventPublisher.publishEvent(new CampusPlaceChangedEvent(place.getId(), CampusPlaceChangeType.UPDATED));
        }
        return Result.ok();
    }

    @Override
    public Result search(CampusPlaceSearchRequest request) {
        try {
            return Result.ok(placeSearchService.search(request));
        } catch (Exception e) {
            log.error("校园地点搜索失败，request={}", request, e);
            return Result.fail("校园地点搜索失败");
        }
    }

    @Override
    public Result queryCampusPlaceByType(Integer categoryId, Integer current, Double x, Double y) {
        if (x == null || y == null) {
            Page<CampusPlace> page = query()
                    .eq("category_id", categoryId)
                    .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
            return Result.ok(page.getRecords());
        }
        int from = (current - 1) * SystemConstants.DEFAULT_PAGE_SIZE;
        int end = current * SystemConstants.DEFAULT_PAGE_SIZE;
        String key = RedisConstants.CAMPUS_PLACE_GEO_KEY + categoryId;
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
            String placeId = result.getContent().getName();
            ids.add(Long.valueOf(placeId));
            return result;
        }).collect(Collectors.toMap(result -> result.getContent().getName(), GeoResult::getDistance));
        String idStr = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        List<CampusPlace> places = query().in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list();
        places.forEach(place -> {
            Distance distance = distanceMap.get(place.getId().toString());
            if (distance != null) {
                place.setDistance(distance.getValue());
            }
        });
        return Result.ok(places);
    }
}

