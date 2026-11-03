package com.nexushub.service;

import com.nexushub.dto.Result;
import com.nexushub.entity.Place;
import com.nexushub.mapper.PlaceMapper;
import com.nexushub.service.impl.PlaceServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.geo.Circle;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlaceServiceImplTest {

    @Test
    void geoQueryShouldLoadPlacesAndAttachDistance() {
        PlaceMapper placeMapper = mock(PlaceMapper.class);
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        GeoOperations<String, String> geoOperations = mock(GeoOperations.class);
        PlaceServiceImpl service = new PlaceServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", placeMapper);
        ReflectionTestUtils.setField(service, "stringRedisTemplate", redisTemplate);

        when(redisTemplate.opsForGeo()).thenReturn(geoOperations);
        RedisGeoCommands.GeoLocation<String> location =
                new RedisGeoCommands.GeoLocation<>("1", new Point(120.1542, 30.2871));
        GeoResult<RedisGeoCommands.GeoLocation<String>> result =
                new GeoResult<>(location, new Distance(0.12, Metrics.KILOMETERS));
        when(geoOperations.radius(eq("geo:place:type:1"), any(Circle.class), any(RedisGeoCommands.GeoRadiusCommandArgs.class)))
                .thenReturn(new GeoResults<>(Arrays.asList(result)));
        when(placeMapper.selectList(any())).thenReturn(Arrays.asList(new Place().setId(1L).setName("街角风味餐厅")));

        Result response = service.queryPlaceByType(1, 1, 120.1542, 30.2871);

        assertThat(response.getSuccess()).isTrue();
        List<Place> places = (List<Place>) response.getData();
        assertThat(places).hasSize(1);
        assertThat(places.get(0).getName()).isEqualTo("街角风味餐厅");
        assertThat(places.get(0).getDistance()).isEqualTo(0.12);
    }
}
