package com.campuslink.service;

import com.campuslink.dto.Result;
import com.campuslink.entity.CampusPlace;
import com.campuslink.mapper.CampusPlaceMapper;
import com.campuslink.service.impl.CampusPlaceServiceImpl;
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

class CampusPlaceServiceImplTest {

    @Test
    void geoQueryShouldLoadPlacesAndAttachDistance() {
        CampusPlaceMapper placeMapper = mock(CampusPlaceMapper.class);
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        GeoOperations<String, String> geoOperations = mock(GeoOperations.class);
        CampusPlaceServiceImpl service = new CampusPlaceServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", placeMapper);
        ReflectionTestUtils.setField(service, "stringRedisTemplate", redisTemplate);

        when(redisTemplate.opsForGeo()).thenReturn(geoOperations);
        RedisGeoCommands.GeoLocation<String> location =
                new RedisGeoCommands.GeoLocation<>("1", new Point(120.1542, 30.2871));
        GeoResult<RedisGeoCommands.GeoLocation<String>> result =
                new GeoResult<>(location, new Distance(0.12, Metrics.KILOMETERS));
        when(geoOperations.radius(eq("geo:place:type:1"), any(Circle.class), any(RedisGeoCommands.GeoRadiusCommandArgs.class)))
                .thenReturn(new GeoResults<>(Arrays.asList(result)));
        when(placeMapper.selectList(any())).thenReturn(Arrays.asList(new CampusPlace().setId(1L).setName("第一食堂")));

        Result response = service.queryCampusPlaceByType(1, 1, 120.1542, 30.2871);

        assertThat(response.getSuccess()).isTrue();
        List<CampusPlace> places = (List<CampusPlace>) response.getData();
        assertThat(places).hasSize(1);
        assertThat(places.get(0).getName()).isEqualTo("第一食堂");
        assertThat(places.get(0).getDistance()).isEqualTo(0.12);
    }
}
