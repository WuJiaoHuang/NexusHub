package com.campuslink;

import com.campuslink.mapper.CampusPlaceMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(properties = "spring.rabbitmq.listener.simple.auto-startup=false")
class CampusLinkApplicationTests {

    @Autowired
    private CampusPlaceMapper placeMapper;

    @Test
    void contextLoadsAndMapperCanQuery() {
        assertThat(placeMapper.selectById(1L)).isNotNull();
    }
}
