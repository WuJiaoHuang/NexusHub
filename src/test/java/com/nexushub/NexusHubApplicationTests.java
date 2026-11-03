package com.nexushub;

import com.nexushub.mapper.PlaceMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(properties = "spring.rabbitmq.listener.simple.auto-startup=false")
class NexusHubApplicationTests {

    @Autowired
    private PlaceMapper placeMapper;

    @Test
    void contextLoadsAndMapperCanQuery() {
        assertThat(placeMapper.selectById(1L)).isNotNull();
    }
}
