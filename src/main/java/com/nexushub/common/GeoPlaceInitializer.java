package com.nexushub.common;

import com.nexushub.entity.Place;
import com.nexushub.service.IPlaceService;
import com.nexushub.utils.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@Profile("!test")
public class GeoPlaceInitializer implements ApplicationRunner {

    @Resource
    private IPlaceService placeService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void run(ApplicationArguments args) {
        List<Place> places = placeService.list();
        Map<Long, List<Place>> placesByCategory = places.stream()
                .filter(place -> place.getCategoryId() != null && place.getX() != null && place.getY() != null)
                .collect(Collectors.groupingBy(Place::getCategoryId));
        placesByCategory.forEach((categoryId, list) -> {
            String key = RedisConstants.PLACE_GEO_KEY + categoryId;
            stringRedisTemplate.delete(key);
            list.forEach(place -> stringRedisTemplate.opsForGeo().add(key,
                    new RedisGeoCommands.GeoLocation<>(place.getId().toString(), new Point(place.getX(), place.getY()))));
        });
        log.info("Redis GEO 地点索引初始化完成，typeCount={}, placeCount={}", placesByCategory.size(), places.size());
    }
}
