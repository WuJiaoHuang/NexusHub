package com.cityeats.common;

import com.cityeats.entity.Shop;
import com.cityeats.service.IShopService;
import com.cityeats.utils.RedisConstants;
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
public class GeoShopInitializer implements ApplicationRunner {

    @Resource
    private IShopService shopService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void run(ApplicationArguments args) {
        List<Shop> shops = shopService.list();
        Map<Long, List<Shop>> shopsByType = shops.stream()
                .filter(shop -> shop.getTypeId() != null && shop.getX() != null && shop.getY() != null)
                .collect(Collectors.groupingBy(Shop::getTypeId));
        shopsByType.forEach((typeId, list) -> {
            String key = RedisConstants.SHOP_GEO_KEY + typeId;
            stringRedisTemplate.delete(key);
            list.forEach(shop -> stringRedisTemplate.opsForGeo().add(key,
                    new RedisGeoCommands.GeoLocation<>(shop.getId().toString(), new Point(shop.getX(), shop.getY()))));
        });
        log.info("Redis GEO 商户索引初始化完成，typeCount={}, shopCount={}", shopsByType.size(), shops.size());
    }
}
